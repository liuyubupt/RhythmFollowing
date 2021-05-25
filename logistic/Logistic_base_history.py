import pandas as pd
import numpy as np

##当前可用
##logistic基于历史的回归逻辑，对于分类界限的选取
def sigmoid(inX):
    s = 1 / (1 + np.exp(-inX))
    return s

def regularize(xMat):
    inMat = xMat.copy()
    inMeans = np.mean(inMat,axis = 0)
    inVar = np.std(inMat,axis = 0)
    inMat = (inMat - inMeans)/inVar
    return inMat

# 梯度下降
def BGD_LR(dataSet, alpha, maxCycles):
    xMat = np.mat(dataSet.iloc[:,:-1].values)
    yMat = np.mat(dataSet.iloc[:,-1].values).T
    xMat = regularize(xMat)
    m,n = xMat.shape
    weights = np.zeros((n, 1))
    for i in range(maxCycles):
        grad = xMat.T * (xMat * weights - yMat) / m
        weights = weights - alpha * grad
    return weights

def main(divide):

    total = 0;
    right = 0;
    total1 = 0;
    right1 = 0;
    # filename = "C:/Users/刘宇/Desktop/data/data_with_history_6wei_5.txt"
    filename = "C:/Users/刘宇/Desktop/毕业设计/data/stepdata6_history.txt"
    dataSet = pd.read_table(filename, header=None, sep=" ")
    # dataSet.columns = ["acceleration", "gyroscope", "labels"]

    # 0.50685 10组
    ws = BGD_LR(dataSet, 0.01, 500)
    print(ws)
    xMat = np.mat(dataSet.iloc[:, :-1].values)
    yMat = np.mat(dataSet.iloc[:, -1].values).T
    # xMat = regularize(xMat)
    try:
        p = sigmoid(xMat * ws).A.flatten()
        for i,j in enumerate(yMat.A.flatten()):
            if j == 0:
                total = total + 1
                if (sigmoid(xMat[i] * ws) < divide):
                    right = right + 1;
            else:
                total1 = total1 + 1
                if (sigmoid(xMat[i] * ws) >= divide):
                    right1 = right1 + 1;
        print("非落地点")
        print(right / total)
        print("落地点")
        print(right1 / total1)
        # file = open("C:/Users/刘宇/Desktop/data/data_with_history_train_result_6wei_5.txt", "w")
        file = open("C:/Users/刘宇/Desktop/毕业设计/data/stepdata6_history_train.txt", "w")
        # file1 = open("C:/Users/刘宇/Desktop/data/real_data_with_history_train_result_6wei_5.txt", "w")
        for i, j in enumerate(p):
            if j < divide:
                p[i] = 0
                file.write("0\n")
            else:
                p[i] = 1
                file.write("1\n")
            # file1.write(str(j) + "\n")
        train_error = (np.fabs(yMat.A.flatten() - p)).sum()
        train_error_rate = train_error / yMat.shape[0]
        # print(train_error)
        print("整体")
        print(1 - train_error_rate)
        file.close()
        print("divide:")
        print(divide)
    except IOError:
        file.close()
        # file1.close()
        print("文件不存在" % filename);

#0.5105
main(0.53)

