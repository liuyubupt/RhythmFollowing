from Logistic_base_history import LogisticBaseHistory;
from FilterFunction import FilterFunction;
# 维度转化函数
# input_file 实际收集到的传感器数据（6维）
# output_file 输出的文件（30维）
# 跑步产生的每一组数据是6维传感器数据，该函数取历史五组数据，将6维数据处理成30维
def dimension_transformation(input_file, output_file):
    try:
        fp = open(input_file, "r");
        done = False;
        list1 = [];
        file = open(output_file, "w")
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
        print("文件不存在" % input_file);

# 逻辑回归算法，进行落地点的判定
# divide 决策边界，经测试0.53效果较好
# input_file 维度转化之后的输出文件
# output_file 逻辑回归的预测数据，与input_file的每一组数据对应，只有0或1
def logistic(divide, input_file, output_file):
    LogisticBaseHistory.main(divide, input_file, output_file);

# 过滤函数，对logistic的预测结果进行过滤处理，离散成最终的离散落地点
# input_file 逻辑回归的预测数据（包含若干数簇1）
# filter_output_file 经过滤函数离散过的数据（离散的数据1）
# offset_output_file 在filter_output_file的基础上经过移位的数据文件，用于后续精确率和召回率的判定
# offset数据向下偏移位数，经测试offset=3，能够得到最好的精确率和召回率
# last_data_file 中间文件，用于经行精确率和召回率的计算，该文件的数据为dimension_transformation函数输出文件的最后一列
# di_output_file dimension_transformation函数输出文件
# mistake 误差范围，如果预测点和实际点的间距不大于mistake则判定为准确，一个点为20ms，则误差为20ms * mistake
def filter_function(input_file, filter_output_file, offset_output_file, offset, last_data_file, di_output_file, mistake):
    FilterFunction.filter(input_file, filter_output_file)
    FilterFunction.offset(filter_output_file, offset_output_file, offset)
    FilterFunction.select([30], di_output_file, last_data_file)
    print("精确率：")
    print( FilterFunction.real_logistic_rate(offset_output_file, last_data_file, mistake))
    print("召回率：")
    print(FilterFunction.real_logistic_rate(last_data_file, offset_output_file, mistake))

def main(di_input_file, di_output_file, divide,
         logistic_output_file,
         filter_output_file, offset_output_file, offset, last_data_file, mistake):
    dimension_transformation(di_input_file, di_output_file)
    logistic(divide, di_output_file, logistic_output_file)
    filter_function(logistic_output_file, filter_output_file, offset_output_file, offset, last_data_file, di_output_file, mistake)


# 输入相关参数，直接运行即可
main("C:/Users/刘宇/Desktop/毕业设计/data/stepdata.txt", "C:/Users/刘宇/Desktop/毕业设计/data/file1.txt", 0.53,
     "C:/Users/刘宇/Desktop/毕业设计/data/file3.txt",
    "C:/Users/刘宇/Desktop/毕业设计/data/file5.txt", "C:/Users/刘宇/Desktop/毕业设计/data/file6.txt", 3,  "C:/Users/刘宇/Desktop/毕业设计/data/file7.txt", 5)