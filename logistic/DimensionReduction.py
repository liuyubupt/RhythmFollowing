# import matplotlib.pyplot as plt
# import numpy as np
##降维处理，三向加速度变为一个（这里应该不需要使用了，数据的整体效果，不做降维效果更好）6维度->2维
try:
    filename = "C:/Users/刘宇/Desktop/毕业设计/data/stepdata2.txt";
    resultname = "C:/Users/刘宇/Desktop/毕业设计/data/stepdata2_6to2.txt"
    fp = open(filename, "r");
    rp = open(resultname, "w");
    done = False;
    while not done:
        aline = fp.readline().strip();
        if (aline != ""):
            res = aline.split(" ");
            res = list(map(float, res))
            num1 = (res[0]*res[0] + res[1]*res[1] + res[2]*res[2]) ** 0.5;
            num2 = (res[3]*res[3] + res[4]*res[4] + res[5]*res[5]) ** 0.5;
            num3 = int(res[6])
            rp.write(str(num1) + " " + str(num2) + " " + str(num3) + "\n");
        else:
            done = True;
except IOError:
    fp.close()
    rp.close()

