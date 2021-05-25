import matplotlib.pyplot as plt
import numpy as np
##降维处理，6维-> 1维
try:
    filename = "/Users/yuliu/Downloads/stepdata2.txt";
    resultname = "/Users/yuliu/Downloads/dimension_reduction_6_to_1.txt"
    fp = open(filename, "r");
    rp = open(resultname, "w");
    done = False;
    while not done:
        aline = fp.readline().strip();
        if (aline != ""):
            res = aline.split(" ");
            res = list(map(float, res))
            num1 = (res[0]*res[0] + res[1]*res[1] + res[2]*res[2]) ** 0.5 + (res[3]*res[3] + res[4]*res[4] + res[5]*res[5]) ** 0.5;
            num2 = int(res[6])
            rp.write(str(num1) + " " + str(num2) + "\n");
        else:
            done = True;
except IOError:
    fp.close()
    rp.close()

