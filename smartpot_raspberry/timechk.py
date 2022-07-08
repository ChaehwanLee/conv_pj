from threading import Thread
import time
import datetime

class timechk(Thread):
    def __init__(self):
        self.now = time.time()
        
    def run(self):
        # 시간 설정값
        # 설정된 모드에 따라서 LED On/OFF 시키는 코드 
        try:
            self.now = time.time()
            while True:
                self.now2 = time.time() - self.now
                print(self.now2/3600) # 시간
        except KeyboardInterrupt:
            pass
        finally:
            pass

if __name__ == "__main__":
    
    currenttime = datetime.datetime.today().hour
    print(currenttime) #1970년 부터 현재까지의 날짜