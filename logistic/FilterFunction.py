#处理回归结果数据，主要逻辑为看到1且与上一个1距离大于15则保留，否则置0，可以满足实时处理
#filename处理文件，
#output_name输出文件
def filter(filename, output_name):
    try:
        file = open(filename, "r")
        out = open(output_name, "w")
        done = True
        count = 12
        while (done):
            aline = file.readline().split("\n")[0]
            if(aline == ""):
                done = False
            elif(aline == "1" and count >= 12):
                count = 0
                out.write("1\n")
            else:
                count += 1
                out.write("0\n")
    except IOError:
        file.close()
        out.close()

#获取文件的行数，用于函数处理文件可靠性的验证
def getLineCount(filename):
    try:
        file = open(filename, "r")
        done = True
        count = 0
        while (done):
            aline = file.readline().split("\n")[0]
            if(aline == ""):
                done = False
            else:
                count += 1
        return count
    except IOError:
        file.close()



#讲过filter处理之后得到离散的数值1，因为观察发现实际数据的落地点在数簇1的末尾，所以filter过滤第一个1之后需要做一个偏移操作，offset为偏移量（大于0）
# 经过offset观察暂取3，即落地点全部向后移动三个点，对应于应用程序则可判定音乐节点延时20*3=60ms
def offset(filename, output_name, offset):
    try:
        file = open(filename, "r")
        out = open(output_name, "w")
        done = True
        count = offset + 1
        while(done):
            aline = file.readline().split("\n")[0]
            if (aline == ""):
                done = False
            elif(aline == "1"):
                out.write("0\n")
                count = 0
            elif(aline == "0"):
                count += 1
                if(count == offset):
                    out.write("1\n")
                else:
                    out.write("0\n")


    except IOError:
        file.close()
        out.close()


#准确度判定（真实样本有的，回归结果是否有）
#判定经过filter和offset的预测点与实际收集到的落地点的吻合程度
#可调整参数，允许误差个数（1个=20ms）
def real_logistic_rate(real_filename, logistic_filename, count):
    try:
        # 记录1的总数
        total = 0
        #记录正确个数
        right = 0
        #记录当前real_filename读到的行数
        position = 0
        real = open(real_filename, "r")
        logistic = open(logistic_filename, "r")
        done = True
        while(done):
            aline = real.readline().split("\n")[0]
            if(aline != ""):
                if(aline == "1"):
                    total += 1
                    for i in range((position - count) * 3, (position + count) * 3 + 1, 3):
                        logistic.seek(i, 0)
                        aline1 = logistic.readline().split("\n")[0]
                        if(aline == aline1):
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
        done1,done2 = True,True
        while(done1 and done1):

            a = file.readline().split("\n")[0]
            b = out.readline().split("\n")[0]

            if(a == ""):
                done1 = False
            if(b == ""):
                done1 = False

            if(a == "1"):
                total += 1
                if(b != "1"):
                    error += 1
        print("error:" + str(error))
        print("total:" + str(total))




    except IOError:
        file.close()
        out.close()

def seek_test():
    try:
        file = open("C:/Users/刘宇/Desktop/data/test.txt", "r")
        file.seek(0, 0)
        print(file.readline())
    except IOError:
        return


def distance(filename, outname):
    try:
        file = open(filename, "r")
        out= open(outname, "w")
        done = True
        count = 0
        while(done):
            a = file.readline().split("\n")[0]
            if(a != ""):
                if(a == "1"):
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

# seek_test()

# same_rate("C:/Users/刘宇/Desktop/data/data_with_history_train_result_6wei_5.txt", "C:/Users/刘宇/Desktop/data/data_with_history_train_result_6wei_5.txt")

#回归0.923826
#实际0.982109
#错后3个点
#divide 0.53

#两个概率
# print("offset = 3 and error = 5")
# filter("C:/Users/刘宇/Desktop/毕业设计/data/data_with_history_train_result_6wei_5.txt", "C:/Users/刘宇/Desktop/毕业设计/data/filter_data_with_history_train_result_6wei_5.txt")
# offset("C:/Users/刘宇/Desktop/毕业设计/data/filter_data_with_history_train_result_6wei_5.txt", "C:/Users/刘宇/Desktop/毕业设计/data/offset_filter_data_with_history_train_result_6wei_5.txt", 3)
# print("以回归结果为标准：")
# print(real_logistic_rate("C:/Users/刘宇/Desktop/毕业设计/data/offset_filter_data_with_history_train_result_6wei_5.txt", "C:/Users/刘宇/Desktop/毕业设计/data/last_data_with_history_6wei_5.txt", 5))
# print("以实际数据为标准：")
# print(real_logistic_rate("C:/Users/刘宇/Desktop/毕业设计/data/last_data_with_history_6wei_5.txt", "C:/Users/刘宇/Desktop/毕业设计/data/offset_filter_data_with_history_train_result_6wei_5.txt", 5))
# print(getLineCount("/Users/yuliu/Downloads/result.txt"))

#24773行
# filter("/Users/yuliu/Downloads/data_with_history_train_result_6wei_5.txt", "/Users/yuliu/Downloads/filter_data_with_history_train_result_6wei_5.txt")
# print(getLineCount("C:/Users/刘宇/Desktop/data/last_data_with_history_6wei_5.txt"))
# offset("/Users/yuliu/Downloads/filter_data_with_history_train_result_6wei_5.txt", "/Users/yuliu/Downloads/offset_filter_data_with_history_train_result_6wei_5.txt", 4)


#data_with_history_6wei_5 原始6*5维数据
#data_with_history_train_result_6wei_5.txt 回归之后的结果预测（仅包含最后一列0/1）
#last_data_with_history_6wei_5.txt 真实数据的最后一列0/1
#filter_data_with_history_train_result_6wei_5.txt  回归数据(数簇1)，经过filter
#offset_filter_data_with_history_train_result_6wei_5.txt filter数据，经过offset（所有1向下偏移）


print("offset = 3 and error = 5")
filter("C:/Users/刘宇/Desktop/毕业设计/data/stepdata6_history_train.txt", "C:/Users/刘宇/Desktop/毕业设计/data/stepdata6_history_train_filter.txt")
offset("C:/Users/刘宇/Desktop/毕业设计/data/stepdata6_history_train_filter.txt", "C:/Users/刘宇/Desktop/毕业设计/data/stepdata6_history_train_filter_offset.txt", 1)
print("精确率：")
print(real_logistic_rate("C:/Users/刘宇/Desktop/毕业设计/data/stepdata6_history_train_filter_offset.txt", "C:/Users/刘宇/Desktop/毕业设计/data/stepdata6_history_lastcolume.txt", 5))
print("召回率：")
print(real_logistic_rate("C:/Users/刘宇/Desktop/毕业设计/data/stepdata6_history_lastcolume.txt", "C:/Users/刘宇/Desktop/毕业设计/data/stepdata6_history_train_filter_offset.txt", 5))
print(getLineCount("C:/Users/刘宇/Desktop/毕业设计/data/stepdata6.txt"))

# distance("C:/Users/刘宇/Desktop/毕业设计/data/last_data_with_history_6wei_5.txt", "C:/Users/刘宇/Desktop/毕业设计/data/distance_last_data_with_history_6wei_5.txt")