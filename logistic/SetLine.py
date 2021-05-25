
# filename = "/Users/yuliu/Downloads/dimension_reduction.txt";
# resultfile = "/Users/yuliu/Downloads/data_with_history.txt"

##当前可用
##为文件标行号
def main(filename, resultfile):
    try:
        fp = open(filename, "r");
        num = 0;
        done = False;
        file = open(resultfile, "w")
        while not done:
            aline = fp.readline().strip();
            if (aline != ""):
                list = aline.split(" ");
                file.write(str(num))
                file.write("   ")
                file.write(aline)
                file.write("\n")
                num += 1

            else:
                done = True;

        fp.close();
        file.close()
    except IOError:
        fp.close();
        file.close()
        print("文件不存在" % filename);

main("C:/Users/刘宇/Desktop/毕业设计/data/stepdata6_history_train.txt", "C:/Users/刘宇/Desktop/毕业设计/data/stepdata6_history_train_num.txt")












