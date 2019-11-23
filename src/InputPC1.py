import serial
import sys
portName = "COM16"
if __name__ == "__main__":
    try:
        myserial = serial.Serial(portName, 115200)
        state = 0
        type_img = {'T': "Top", 'B': "Bottom", 'L': "Left",
                    'R': "Right", 'U': "Upper", 'D': "Lower"}
        img_angle = []
        while True:
            print("--------------------------------")
            command = input(
                "Enter \"Start\" for Activate\n" if state ==
                0 else "Enter Type Image for Capture and Receive Data\n" + img_angle.__str__() + "\n")
            if state == 0 and command.lower() == "start":
                myserial.write(b'S')
                img_angle = []
                for _ in range(3):
                    angle = int.from_bytes(myserial.read(), "big")
                    angle = angle if angle < 128 else - (256 - angle)
                    typeImgage = type_img[myserial.read().decode("utf-8")]
                    print("Angle", angle.__str__().center(
                        5), "Type Image", typeImgage)
                    img_angle.append(typeImgage)
                state = 1
            elif state == 1 and command in img_angle:
                for key, value in type_img.items():
                    if value.lower() == command.lower():
                        myserial.write(ord(key).to_bytes(1,"big"))
                        for c in range(16):
                            posx = ((0x1F & int.from_bytes(myserial.read(),"big") | (int.from_bytes(myserial.read(),"big") & 0xF) << 5))
                            posy = ((0x1F & int.from_bytes(myserial.read(),"big") | (int.from_bytes(myserial.read(),"big") & 0xF) << 5))
                            mypixelValue = int.from_bytes(myserial.read(),"big")
                            print("pixel x = ",posx,"pixel y = ",posy,"pixel value =",mypixelValue)
                state = 0
            else:
                print("Command is not correct re-Enter for Active")

    except serial.SerialException:
        print("Port busy", file=sys.__stderr__)
