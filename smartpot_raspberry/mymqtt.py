import signal
import paho.mqtt.client as mqtt
from threading import Thread, Event
import spithread
import RPi.GPIO as gpio
from image_ras import onceCamera
from waterPump import waterPump
from led_OnOff import ledOnOff
import time
import datetime
import paho.mqtt.publish as publisher


class MqttWorker:
    def __init__(self):
        self.Disease = ""  # 받은 질병을 저장할 변수
        self.client = mqtt.Client()
        self.client.on_connect = self.on_connect
        self.client.on_message = self.on_message
        self.spisensor = spithread.sensorval()
        self.spisensor.start()
        self.exit_event = Event()
        self.waterPump = waterPump()
        self.camera = onceCamera()
        self.led = ledOnOff()
        self.camera.start()

        # pot1의 초기 설정값
        self.mode = ""  # 식물 종류
        self.growmode = 7  # 생육 상태 기본상태가 7
        self.disease = ""  # 질병 상태
        self.waterSpoutValue = 50  # 초기 물 공급 양
        self.ledOnTimeMode = 0  # LED를 켜기 위한 코드
        self.soilWaterLevel = 0.0  # 토양습도 제한 값

        self.count = 0
        self.watercount = 0


    def signal_handler(self, signum, frame):
        self.exit_event.set()
        gpio.cleanup()
        if self.exit_event.is_set() == True:
            exit(0)

    def mymqtt_connect(self):
        try:
            print("브로커 연결 시작하기")
            self.client.connect("3.96.178.99", 1883, 60)
            signal.signal(signal.SIGINT, self.signal_handler)
            mythreadobj = Thread(target=self.client.loop_forever)
            mythreadobj.start()
        except KeyboardInterrupt:
            pass
        finally:
            print("종료")

    def on_connect(self, client, userdate, flags, rc):
        print("connect..." + str(rc))
        if rc == 0:
            client.subscribe("iot/#")
            client.subscribe("mode/#")  # 안드로이드로부터 식물명이 들어오는 토픽을 구독
        else:
            print("연결실패......")

    # 현재 물 높이를 가져오는 메소드
    def getWaterData(self):
        waterLevelData = self.spisensor.getWaterLevel()
        return waterLevelData

    def getSoilWaterData(self):
        soilWaterData = self.spisensor.getSoilLevel()
        return soilWaterData

    # 토양 수분 값이 식물에 따른 값 이하로 감소하면 물을 정해진 만큼 공급하는 메소드 (스레드)
    def waterPumpAct(self):
        try:
            self.count = 0
            while True:
                if (self.getSoilWaterData() < self.soilWaterLevel):  # 토양 수분 값이 정해진 제한 값 보다 작아지면
                    currentWaterLevel = int(self.getWaterData() * 5)  # 맨 처음 물 양을 저장 500ML 기준
                    if currentWaterLevel < self.waterSpoutValue:  # 물이 특정 값보다 적으면 동작하지 않음
                        print("물이 적음")
                        # 안드로이드로 물이 부족하다는 것을 전송하는 코드 작성
                        publisher.single("sensor1/waterWarn", "물부족", hostname="3.96.178.99")
                        time.sleep(1)
                    else:  # 물이 특정 값보다 커지면 (정상상태)
                        while (currentWaterLevel - int(
                                self.getWaterData() * 5) < self.waterSpoutValue):  # 설정된 값보다 빠진 물이 작은동안만 무한루프
                            if self.count == 0:
                                self.waterPump.waterOpen()  # 펌프 오픈(1번만)
                                self.count = 1
                            time.sleep(0.5)
                        self.waterPump.waterOff()  # 무한루프에서 나오면 펌프를 닫음
                        self.count = 0
                time.sleep(1)
        except KeyboardInterrupt:
            pass
        finally:
            pass

    # 처음 물 양에서 현재의 물 양을 뺀 값이 정해진 값보다 작은 경우 무한 루프
    def pubLevelDisease(self):  # 5초 간격으로 생육 상태 + 질병 상태를 보내는 메소드
        try:
            while True:
                publisher.single("sensor1/growmode", str(self.growmode) + ":" + self.disease, hostname="3.96.178.99")
                time.sleep(5)
        except KeyboardInterrupt:
            pass
        finally:
            pass

    # def Led10Minute(self):
    #     # 시간 설정값
    #     # 설정된 모드에 따라서 LED On/OFF 시키는 코드
    #     try:
    #         while True:
    #             self.nowhour = datetime.datetime.today().hour # 현재 시간 (분 단위)
    #             self.nowmin = datetime.datetime.today().minute
    #             self.nowsec = datetime.datetime.today().second
    #             while(0<=datetime.datetime.today().hour<=7 or 19<=datetime.datetime.today().hour<=23): #정해진 시간동안
    #                 if (self.ledOn10MinFirstSaveFlag == 0):  # 50분 off 시작시간이 저장된 경우
    #                     if(datetime.datetime.today().minute == 0 and datetime.datetime.today().second == 0): # 현재 시간이 19:00:00 인 경우( 10분 On 시작 전 )
    #                         self.starttime = datetime.datetime.today().minute # 시작시간 저장
    #                         self.led.ledOn() # ( 10분 On 시작 )
    #                         self.ledOn10MinFlag = 1
    #                 if(self.ledOn10MinFirstSaveFlag == 1): # 시작시간이 저장된 경우(10 on)  ledOn10MinFirstSaveFlag = 1
    #                     if(datetime.datetime.today().minute - self.starttime == 10): # 10분 지남
    #                         self.led.ledOff()
    #                         self.ledOn10MinFlag = 0
    #                 time.sleep(1)
    #     except KeyboardInterrupt:
    #         pass
    #     finally:
    #         pass

    def on_message(self, client, userdata, message):
        try:
            # 레벨 액티비티에서 카메라 동작시
            if message.topic == "iot/actLevelCamera":
                print("1111111111111111111111111111")
                self.camera.opCamera("levelDisease", self.mode)

            # 질병 액티비티에서 카메라 동작시
            elif message.topic == "iot/actDiseaseCamera":
                print("2")
                self.camera.opCamera("levelDisease", self.mode)

                # AI에서 IOT로 돌아오는 코드 -> 여기서 생육상태가 변경 될 예정
            elif message.topic == "iot/aiToIotValue":
                print("3")
                val = message.payload.decode("utf-8").split('/')
                print(val[0] + val[1] + ".")
                self.growmode = int(val[0])# 생육상태 + 질병상태 변수를 변경
                if self.growmode == 1:
                    self.waterSpoutValue = 100
                    self.ledOnTimeMode = 1
                    self.soilWaterLevel = 80.0
                elif self.growmode == 2:
                    self.waterSpoutValue = 50
                    self.ledOnTimeMode = 1
                    self.soilWaterLevel = 85.0
                elif self.growmode == 3:
                    self.waterSpoutValue = 50
                    self.ledOnTimeMode = 1
                    self.soilWaterLevel = 85.0
                elif self.growmode == 4:
                    self.waterSpoutValue = 100
                    self.ledOnTimeMode = 1
                    self.soilWaterLevel = 85.0
                elif self.growmode == 5:
                    self.waterSpoutValue = 100
                    self.ledOnTimeMode = 1
                    self.soilWaterLevel = 85.0
                elif self.mode == "Lettuce" and self.growmode == 1:
                    self.growmode = 8  # 상추 1단계 - 8 상추 2단계 - 9
                    self.waterSpoutValue = 100
                    self.ledOnTimeMode = 2
                    self.soilWaterLevel = 85.0
                elif self.mode == "Lettuce" and self.growmode == 2:
                    self.growmode = 9  # 상추 1단계 - 8 상추 2단계 - 9
                    self.waterSpoutValue = 100
                    self.ledOnTimeMode = 2
                    self.soilWaterLevel = 85.0
                elif self.growmode == 7 and self.mode == "Rosemary":
                    self.waterSpoutValue = 150
                    self.ledOnTimeMode = 3
                    self.soilWaterLevel = 85.0
                elif self.growmode == 7 and self.mode == "Geranium":
                    self.waterSpoutValue = 200
                    self.ledOnTimeMode = 4
                    self.soilWaterLevel = 90.0
                self.disease = val[1]
                self.led.setLed(1,4) # led 설정변경
            # 화분의 식물 수정 시 정보를 변경하는 메소드
            elif message.topic == "mode/pot1PlantModify":
                val = message.payload.decode("utf-8")
                self.mode = val
                if self.mode == "Strawberry":  # 식물이 딸기인 경우
                    self.growmode = 0  # 생육상태를 0단계로 변경 -> 아니면 7으로 고정
                    self.waterSpoutValue = 50
                    self.ledOnTimeMode = 1
                    self.soilWaterLevel = 80.0
                elif self.mode == "Lettuce":
                    self.growmode = 8  # 상추 1단계 - 6 상추 2단계 - 7
                    self.waterSpoutValue = 100
                    self.ledOnTimeMode = 2
                    self.soilWaterLevel = 85.0
                elif self.mode == "Rosemary":
                    self.growmode = 7
                    self.waterSpoutValue = 150
                    self.ledOnTimeMode = 3
                    self.soilWaterLevel = 85.0
                elif self.mode == "Geranium":
                    self.growmode = 7
                    self.waterSpoutValue = 200
                    self.ledOnTimeMode = 4
                    self.soilWaterLevel = 90.0
                self.led.setLed(self.ledOnTimeMode,self.growmode)
                self.camera.opCamera("levelDisease", self.mode)  # 이미지를 한번 서버로 보내줌으로서 디렉토리 생성
            # 앱에서 처음 화분을 설정한 경우
            elif message.topic == "mode/pot1PlantName":
                val = message.payload.decode("utf-8")
                self.mode = val  # 식물 모드를 변경
                if self.mode == "Strawberry":  # 식물이 딸기인 경우
                    self.growmode = 1  # 생육상태를 0단계로 변경 -> 아니면 7으로 고정
                    self.waterSpoutValue = 50
                    self.ledOnTimeMode = 1
                    self.soilWaterLevel = 80.0
                elif self.mode == "Lettuce":
                    self.growmode = 8  # 상추 1단계 - 6 상추 2단계 - 7
                    self.waterSpoutValue = 100
                    self.ledOnTimeMode = 2
                    self.soilWaterLevel = 85.0
                elif self.mode == "Rosemary":
                    self.growmode = 7
                    self.waterSpoutValue = 150
                    self.ledOnTimeMode = 3
                    self.soilWaterLevel = 85.0
                elif self.mode == "Geranium":
                    self.growmode = 7
                    self.waterSpoutValue = 200
                    self.ledOnTimeMode = 4
                    self.soilWaterLevel = 90.0
                print(self.mode + str(self.waterSpoutValue) + "\n")
                # led 동작 스레드 시작
                self.led.setLed(self.ledOnTimeMode, self.growmode)
                self.led.start()
                # 생육 + 질병상태 전송 스레드 시작
                self.levelDisease = Thread(target=self.pubLevelDisease)
                self.levelDisease.start()
                # 워터펌프 스레드 시작
                self.waterpumpthread = Thread(target=self.waterPumpAct)
                self.waterpumpthread.start()
                self.camera.opCamera("levelDisease", self.mode)  # 이미지를 한번 서버로 보내줌으로서 디렉토리 생성
            else:
                val = message.payload.decode("utf-8")
                print(val)
        except:
            pass
        finally:
            pass