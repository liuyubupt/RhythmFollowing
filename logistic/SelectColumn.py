#当前可用
#一般用来抽取最后的0、1结果
##抽取每行的若干列
def select(list, filename, output_name):
    try:
        fp = open(filename, "r")
        out = open(output_name, "w")
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
        print("文件不存在" % filename)

# select([30], "C:/Users/刘宇/Desktop/data/data_with_history_6wei_5.txt", "C:/Users/刘宇/Desktop/data/last_data_with_history_6wei_5.txt")


select([30], "C:/Users/刘宇/Desktop/毕业设计/data/stepdata6_history.txt", "C:/Users/刘宇/Desktop/毕业设计/data/stepdata6_history_lastcolume.txt")

