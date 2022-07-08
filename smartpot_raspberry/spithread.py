from threading import Thread
from threading import Event
from distutils.log import error
import paho.mqtt.publish as publisher
import spidev
import signal
import time
import RPi.GPIO as GPIO
import board  # 데이터 송신용 board모듈
import adafruit_dht
import smbus  # i2c 라이브러리
from getTemHumid import getdht11


class sensorval(Thread):
    def __init__(self):

        GPIO.setmode(GPIO.BCM)
        self.getdht11 = getdht11()
        self.humidity_data = 66
        self.temperature_data = 20
        # BH1750
        self.I2C_CH = 1
        self.BH1750_DEV_ADDR = 0x23
        self.CONT_H_RES_MODE = 0x10
        self.CONT_H_RES_MODE2 = 0x11
        self.CONT_L_RES_MODE = 0x13
        self.ONETIME_H_RES_MODE = 0x20
        self.ONETIME_H_RES_MODE2 = 0x21
        self.ONETIME_L_RES_MODE = 0x23
        self.i2c = smbus.SMBus(self.I2C_CH)

        self.delay = 1
        self.pot_channel1 = 0
        self.pot_channel2 = 1
        self.spi = spidev.SpiDev()
        self.spi.open(0, 0)
        self.spi.max_speed_hz = 100000
        self.exit_event = Event()
        self.getdht11.start()
        Thread.__init__(self)
        signal.signal(signal.SIGINT, self.signal_handler)

    def signal_handler(self, signum, frame):
        self.exit_event.set()
        GPIO.cleanup()
        if self.exit_event.is_set() == True:
            exit(0)

    def readadc(self, adcnum):
        if adcnum < 0 or adcnum > 7:
            return -1
        r = self.spi.xfer2([1, 8 + adcnum << 4, 0])
        data = ((r[1] & 3) << 8) + r[2]
        return data

    def getWaterLevel(self):
        getwaterLevel = self.waterLevel
        getnew_waterLevel_value = round((((getwaterLevel - 0) / (650 - 0)) * (100 - 0) + 0), 2)
        return getnew_waterLevel_value

    def getSoilLevel(self):
        getSoilLevel = self.soilWater
        getnew_soilWater_value = round((((getSoilLevel - 0) / (700 - 0)) * (100 - 0) + 0), 2)
        return getnew_soilWater_value

    def run(self):
        try:
            while True:
                # 수위(접촉), 토양습도, 조도 센서에서 값을 얻을 예정
                luxBytes = self.i2c.read_i2c_block_data(self.BH1750_DEV_ADDR, self.CONT_H_RES_MODE, 2)

                # 바이트 배열을 int로 변환
                lux = int.from_bytes(luxBytes, byteorder='big')

                self.waterLevel = self.readadc(self.pot_channel1)
                self.soilWater = self.readadc(self.pot_channel2)
                # 토양 수분 값을 0~100으로 변환하는 코드
                new_soilWater_value = round((((self.soilWater - 0) / (700 - 0)) * (100 - 0) + 0), 2)
                new_waterLevel_value = round((((self.waterLevel - 0) / (650 - 0)) * (100 - 0) + 0), 2)

                # 물 주는 버튼을 꼭 사용해야 할까? -> 자동으로 주면 되는데
                publisher.single("sensor1/every",
                                 str(new_waterLevel_value) + ":" + str(new_soilWater_value) + ":" + str(
                                     self.getdht11.gettemp11()) + ":" + str(lux) + ":" + str(
                                     self.getdht11.gethumid11()), hostname="3.96.178.99")
                print("---------------------------")
                print("waterLevel: %d" % self.waterLevel)
                print("soilWater: %d" % self.soilWater)
                print("humidity: %f" % self.getdht11.gethumid11())
                print("temperature: %f" % self.getdht11.gettemp11())
                print("lux: %d" % lux)

                time.sleep(2)
        except KeyboardInterrupt:
            pass
        finally:
            GPIO.cleanup()