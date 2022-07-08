import time
import RPi.GPIO as GPIO
from threading import Thread
import datetime
from threading import Event
import signal


class ledOnOff(Thread):
    def __init__(self):
        Thread.__init__(self)
        self.starttime = None
        GPIO.setmode(GPIO.BCM)
        # 워터펌프 릴레이 핀 출력설정 코드 작성
        self.LED_PIN = 26
        GPIO.setup(self.LED_PIN, GPIO.OUT)
        GPIO.output(self.LED_PIN, GPIO.HIGH)
        self.exit_event = Event()
        self.ledOn10MinFirstSaveFlag = 0
        signal.signal(signal.SIGINT, self.signal_handler)

    def signal_handler(self, signum, frame):
        self.exit_event.set()
        GPIO.cleanup()
        if self.exit_event.is_set() == True:
            exit(0)

    # 식물에 따른 시간 설정
    def setLed(self, mode, growmode):
        if mode == 1: # 딸기
            self.mode = 1
            self.growmode = growmode
            print(self.growmode)
            self.LedOnTime = 7
            self.LedOffTime = 16
        elif mode == 2: # 상추
            self.growmode = 7
            self.mode = 2
            self.LedOnTime = 8
            self.LedOffTime = 18
        elif mode == 3: # 로즈마리
            self.growmode = 7
            self.mode = 3
            self.LedOnTime = 6
            self.LedOffTime = 16
        elif mode == 4: # 제라늄
            self.growmode = 7
            self.mode = 4
            self.LedOnTime = 9
            self.LedOffTime = 19
        elif mode == 0:  # 테스트용
            self.LedOnTime = 17
            self.LedOffTime = 15
        print("LEDON : %d" % self.LedOnTime)
        print("LEDOFF : %d" % self.LedOffTime)

    def ledOn(self):
        GPIO.setmode(GPIO.BCM)
        GPIO.setup(self.LED_PIN, GPIO.OUT)
        GPIO.output(self.LED_PIN, GPIO.LOW)

    def ledOff(self):
        GPIO.setmode(GPIO.BCM)
        GPIO.setup(self.LED_PIN, GPIO.OUT)
        GPIO.output(self.LED_PIN, GPIO.HIGH)

    def run(self):  # LED On 시키기
        count = 0
        try:
            while True:
                if(self.mode != 1): #딸기 제외
                    GPIO.setmode(GPIO.BCM)
                    GPIO.setup(self.LED_PIN, GPIO.OUT)
                    currentTime = datetime.datetime.today().hour
                    onTime = self.LedOnTime
                    offTime = self.LedOffTime
                    if currentTime == onTime:
                        if count == 0:
                            GPIO.output(self.LED_PIN, GPIO.LOW)
                            count == 1
                            print("led 켜짐")
                    elif currentTime == offTime:
                        if count == 0:
                            GPIO.output(self.LED_PIN, GPIO.HIGH)
                            count == 1
                            print("led 꺼짐")
                    else:
                        count = 0
                    # print("LEDON : %d" % self.LedOnTime)
                    # print("LEDOFF : %d" % self.LedOffTime)
                    time.sleep(1)
                elif self.mode == 1 and self.growmode == 4 or self.growmode == 5:
                    
                    while 0 <= datetime.datetime.today().hour <= 7 or 19 <= datetime.datetime.today().hour <= 23:  # 정해진 시간동안
                        if self.ledOn10MinFirstSaveFlag == 0:  # 50분 off 시작시간이 저장된 경우
                            if datetime.datetime.today().minute == 00 and datetime.datetime.today().second == 00:  # 현재 시간이 19:00:00 인 경우( 10분 On 시작 전 )
                                self.starttime = datetime.datetime.today().minute  # 시작시간 저장
                                self.ledOn()  # ( 10분 On 시작 )
                                self.ledOn10MinFirstSaveFlag = 1
                                print("led 켜짐")
                        if self.ledOn10MinFirstSaveFlag == 1:  # 시작시간이 저장된 경우(10 on)
                            if datetime.datetime.today().minute - self.starttime == 10:  # 10분 지남
                                self.ledOff()
                                self.ledOn10MinFirstSaveFlag = 0
                                print("led 꺼짐")
                        time.sleep(1)
                    time.sleep(1)
        except KeyboardInterrupt:
            pass
        finally:
            GPIO.cleanup()


if __name__ == "__main__":
    try:

        ledTest = ledOnOff()
        ledTest.setLed(1,4)
        ledTest.start()
    except KeyboardInterrupt:
        pass
    finally:
        GPIO.cleanup()