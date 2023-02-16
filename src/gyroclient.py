from sense_hat import SenseHat
from time import sleep
import socket
import json

sense = SenseHat()
sense.clear()

j = open("./config.json", 'r')
data = json.loads(j.read())
host = data["ip"]
port = int(data["port"])

client = socket.socket()
client.connect((host, port))
CW_BOUND = 65
CCW_BOUND = 360 - CW_BOUND

def map(val, old_bottom, old_top, new_bottom, new_top):
	diffVOB = val - old_bottom
	diffOTB = old_top - old_bottom
	diffNTB = new_top - new_bottom

	return int((diffVOB * diffNTB / diffOTB) + new_bottom)

'''
Diagram of how roll is mapped by the sense hat:
                  360 | 0     
   CCW_BOUND + x      |      CW_BOUND - x
                \     |     /
                 \    |    /
                  \   |   /
CCW_BOUND--------[Sense Hat]--------CW_BOUND

The goal is to remap these angles such that
they fit this form:
                      90
           0 + y      |      180 - y
                \     |     /
                 \    |    /
        	  \   |	  /
0----------------[Sense Hat]-------------180

So that turning the hat in the CCW direction won't
cause the servo motor to flip sides suddenly
'''

try:

	while True:

		o = sense.get_orientation()
		roll: int = round(o["roll"])

		if CCW_BOUND <= roll <= 360:
			roll = map(roll, CCW_BOUND, 360, 0, 90)
		elif 0 <= roll <= CW_BOUND:
			roll = map(roll, 0, CW_BOUND, 90, 180)
		elif CW_BOUND < roll < CCW_BOUND:
			continue

		print(roll)
		client.send(f"{roll}\r\n".encode())
		sleep(0.05)
except ConnectionResetError:
	print("Connection forcibly closed by server.")
except KeyboardInterrupt:
	client.send("kill server".encode())
	client.close()
