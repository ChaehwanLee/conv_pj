
import RPi.GPIO as GPIO
class waterPump():
    def __init__(self):
        GPIO.setmode(GPIO.BCM)
        #워터펌프 릴레이 핀 출력설정 코드 작성
        self.relay_pin = 16
        GPIO.setup(self.relay_pin,GPIO.OUT) 
        GPIO.output(self.relay_pin , GPIO.HIGH)
            
    def waterOpen(self):
        #릴레이에 연결된 입출력 핀을 물 양을 수위센서로 측정하여 뿌리기
        #현재는 동작시 릴레이만 동작
        GPIO.output(self.relay_pin , GPIO.LOW)
        
        
    def waterOff(self):
        #릴레이에 연결된 입출력 핀을 물 양을 수위센서로 측정하여 뿌리기
        #현재는 동작시 릴레이만 동작
        GPIO.output(self.relay_pin , GPIO.HIGH)
       
            

        
        