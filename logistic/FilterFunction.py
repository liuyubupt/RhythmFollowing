
class FilterFunction():
    def filter(input_file, output_name):
        try:
            file = open(input_file, "r")
            out = open(output_name, "w")
            done = True
            count = 12
            while (done):
                aline = file.readline().split("\n")[0]
                if (aline == ""):
                    done = False
                elif (aline == "1" and count >= 12):
                    count = 0
                    out.write("1\n")
                else:
                    count += 1
                    out.write("0\n")
        except IOError:
            file.close()
            out.close()

    # 获取文件的行数，用于函数处理文件可靠性的验证
    def getLineCount(filename):
        try:
            file = open(filename, "r")
            done = True
            count = 0
            while (done):
                aline = file.readline().split("\n")[0]
                if (aline == ""):
                    done = False
                else:
                    count += 1
            return count
        except IOError:
            file.close()

    # 讲过filter处理之后得到离散的数值1，因为观察发现实际数据的落地点在数簇1的末尾，所以filter过滤第一个1之后需要做一个偏移操作，offset为偏移量（大于0）
    # 经过offset观察暂取3，即落地点全部向后移动三个点，对应于应用程序则可判定音乐节点延时20*3=60ms
    def offset(input_file, output_file, offset):
        try:
            file = open(input_file, "r")
            out = open(output_file, "w")
            done = True
            count = offset + 1
            while (done):
                aline = file.readline().split("\n")[0]
                if (aline == ""):
                    done = False
                elif (aline == "1"):
                    out.write("0\n")
                    count = 0
                elif (aline == "0"):
                    count += 1
                    if (count == offset):
                        out.write("1\n")
                    else:
                        out.write("0\n")


        except IOError:
            file.close()
            out.close()

    # 准确度判定（真实样本有的，回归结果是否有）
    # 判定经过filter和offset的预测点与实际收集到的落地点的吻合程度
    # 可调整参数，允许误差个数（1个=20ms）
    def real_logistic_rate(real_filename, logistic_filename, count):
        try:
            # 记录1的总数
            total = 0
            # 记录正确个数
            right = 0
            # 记录当前real_filename读到的行数
            position = 0
            real = open(real_filename, "r")
            logistic = open(logistic_filename, "r")
            done = True
            while (done):
                aline = real.readline().split("\n")[0]
                if (aline != ""):
                    if (aline == "1"):
                        total += 1
                        for i in range((position - count) * 3, (position + count) * 3 + 1, 3):
                            logistic.seek(i, 0)
                            aline1 = logistic.readline().split("\n")[0]
                            if (aline == aline1):
                                right += 1
                                break
                    position += 1
                else:
                    done = False
            return right / total



        except IOError:
            real.close()
            logistic.close()

    def same_rate(filename, output_name):
        try:
            total = 0;
            error = 0;
            file = open(filename, "r")
            out = open(output_name, "r")
            done1, done2 = True, True
            while (done1 and done1):

                a = file.readline().split("\n")[0]
                b = out.readline().split("\n")[0]

                if (a == ""):
                    done1 = False
                if (b == ""):
                    done1 = False

                if (a == "1"):
                    total += 1
                    if (b != "1"):
                        error += 1
            print("error:" + str(error))
            print("total:" + str(total))




        except IOError:
            file.close()
            out.close()

    def seek_test(self):
        try:
            file = open("C:/Users/刘宇/Desktop/data/test.txt", "r")
            file.seek(0, 0)
            print(file.readline())
        except IOError:
            return

    def distance(filename, outname):
        try:
            file = open(filename, "r")
            out = open(outname, "w")
            done = True
            count = 0
            while (done):
                a = file.readline().split("\n")[0]
                if (a != ""):
                    if (a == "1"):
                        out.write(str(count) + "\n")
                        count = 0
                    else:
                        count += 1
                else:
                    done = False
        except IOError:
            file.close()
            out.close()
            return

    # 抽去文件数据的若干列
    # list形式[25,30]表示抽取第25列和第30列
    def select(list, input_file, output_file):
        try:
            fp = open(input_file, "r")
            out = open(output_file, "w")
            done = False
            while not done:
                aline = fp.readline().split("\n")[0].split(" ")
                if (len(aline) > 1):
                    msg = ""
                    for index in list:
                        msg += str(aline[index]) + " "
                    out.write(msg.strip() + "\n")

                else:
                    done = True

            fp.close()
        except IOError:
            fp.close()
            out.close()
            print("文件不存在" % input_file)
