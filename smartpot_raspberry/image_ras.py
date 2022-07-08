from picamera import PiCamera
from time import sleep
import paho.mqtt.publish as publisher
from threading import Thread
import datetime
import time

class onceCamera(Thread):
    def __init__(self):
        self.camera = PiCamera()
        self.camera.resolution = (2592,1944)
        Thread.__init__(self)
        self.plantname = ""
        print("카메라 연결 완료")
        
    def opCamera(self,mode,plantname):
        self.plantname = plantname
        self.camera.start_preview()
        sleep(5)
        self.camera.capture("/home/pi/mywork/basic/image_prac/test"+"10"+".jpg")
        f = open("/home/pi/mywork/basic/image_prac/test"+"10"+".jpg", 'rb')
        a = f.read()
        fimage = bytearray(a)
        f.close
        print(str(plantname))
        if mode == "periodic": # 주기적으로 보낼 때 동작하는 코드
            publisher.single("iot/awsperiodic/"+plantname,fimage,hostname="3.96.178.99")
        elif mode == "levelDisease": # 레벨이나 질병 페이지에서 신호를 보낼 때 동작하는 코드
            publisher.single("iot/awslevelDisease/"+plantname,fimage,hostname="3.96.178.99")
        self.camera.stop_preview()
     
    
    # def run(self):
    #     try:
    #         count = 0
    #         while True:
    #             self.currenttime = datetime.datetime.today().hour
    #             if count == 0:
    #                 if self.currenttime == 13: # 13시에 주기적으로 한번씩 보냄
    #                     self.opCamera("periodic",self.plantname)
    #                     print("awspubed")
    #                     count = 1
    #                 else:
    #                     count = 0
    #             time.sleep(60)
                
    #     except KeyboardInterrupt:
    #         pass
    #     finally:
    #         self.camera.close()
            
if __name__ == "__main__":
    onceCamera1 = onceCamera()
    onceCamera1.start()

            

        
        