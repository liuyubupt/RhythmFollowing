
filename = "C:/Users/刘宇/Desktop/毕业设计/data/stepdata6.txt";
resultfile = "C:/Users/刘宇/Desktop/毕业设计/data/stepdata6_history.txt"

##当前可用
##单组数据变多组数据（基于历史数据估算当前数据，选择历史的5组数据，整体回归结果表现更好）

#6维变30维
try:
    fp = open(filename, "r");
    done = False;
    list1 = [];
    file = open(resultfile, "w")
    while not done:
        aline = fp.readline().strip();
        if (aline != ""):
            list = aline.split(" ");
            list1.append(list[0]);
            list1.append(list[1]);
            list1.append(list[2]);
            list1.append(list[3]);
            list1.append(list[4]);
            list1.append(list[5]);
            if (len(list1) == 30):
                print(list1)
                file.write(" ".join(list1))
                file.write(" ")
                file.write(list[6])
                file.write("\n")
                list1 = list1[6:]
        else:
            done = True;

    fp.close();
    file.close()
except IOError:
    fp.close();
    file.close()
    print("文件不存在" % filename);



#二维变十维


# filename = "C:/Users/刘宇/Desktop/毕业设计/data/stepdata2_6to2.txt";
# resultfile = "C:/Users/刘宇/Desktop/毕业设计/data/stepdata2_6to2_history.txt"
#
# try:
#     fp = open(filename, "r");
#     done = False;
#     list1 = [];
#     file = open(resultfile, "w")
#     while not done:
#         aline = fp.readline().strip();
#         if (aline != ""):
#             list = aline.split(" ");
#             list1.append(list[0]);
#             list1.append(list[1]);
#             if (len(list1) == 10):
#                 print(list1)
#                 file.write(" ".join(list1))
#                 file.write(" ")
#                 file.write(list[2])
#                 file.write("\n")
#                 list1 = list1[2:]
#         else:
#             done = True;
#
#     fp.close();
#     file.close()
# except IOError:
#     fp.close();
#     file.close()
#     print("文件不存在" % filename);








