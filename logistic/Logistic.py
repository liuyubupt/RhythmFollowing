import pandas as pd
import numpy as np




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

def main():
    total = 0;
    right = 0;
    filename = "C:/Users/刘宇/Desktop/毕业设计/data/stepdata2_6to2.txt"
    dataSet = pd.read_table(filename, header=None, sep=" ")
    # dataSet.columns = ["acceleration", "gyroscope", "labels"]


    ws = BGD_LR(dataSet, 0.01, 500)
    xMat = np.mat(dataSet.iloc[:, :-1].values)
    yMat = np.mat(dataSet.iloc[:, -1].values).T
    xMat = regularize(xMat)
    p = sigmoid(xMat * ws).A.flatten()
    # for i,j in enumerate(yMat.A.flatten()):
    #     if j == 0:
    #         total = total + 1
    #         if (sigmoid(xMat[i] * ws) < 0.49):
    #             right = right + 1;
    # print(right / total)
    file = open("C:/Users/刘宇/Desktop/毕业设计/data/stepdata2_6to2_predict.txt", "w")
    try:
        for i, j in enumerate(p):
            if j < 0.50:
                p[i] = 0
                file.write("0\n")
            else:
                p[i] = 1
                file.write("1\n")

    except IOError:
        file.close()
        # file1.close()
        print("文件不存在" % filename);

    train_error = (np.fabs(yMat.A.flatten() - p)).sum()
    train_error_rate = train_error / yMat.shape[0]
    print(train_error)
    print(1 - train_error_rate)


main()

