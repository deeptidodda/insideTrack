# -*- coding: utf-8 -*-
"""
Created on Thu Nov  8 14:04:34 2018
@author: atp1ksb
"""
# Importing the libraries
import numpy as np
import matplotlib.pyplot as plt
import pandas as pd
import glob
import time
from sklearn.preprocessing import LabelEncoder , OneHotEncoder
from sklearn.model_selection import train_test_split 
from sklearn.preprocessing import StandardScaler
from  sklearn.compose import ColumnTransformer 
from sklearn.compose import make_column_transformer

#from future_encoders import ColumnTransformer

#

l = [pd.read_csv(filename , memory_map =True,na_filter=False,
                 usecols=['ORAC1','ORAC2','ORAC3','NUM_CONNECTIONS','TOTAL_DUR_MIN']) 
    for filename in glob.glob("//mnt//non-ssd//Itenaries//*.csv")]
dataset = pd.concat(l, axis=0)

m = [pd.read_csv(filename , memory_map =True, na_filter=False,
                 usecols=['INCLUDE']) 
    for filename in glob.glob("//mnt//non-ssd//Itenaries//*.csv")]
dataset = pd.concat(l, axis=0)
output_data= pd.concat(m, axis=0)

#dataset = pd.read_csv('O:\\git\\insideTrack\\data\\itineraries1.csv' ,na_filter=False,
#                      usecols=['ORAC1','ORAC2','ORAC3','NUM_CONNECTIONS','TOTAL_DUR_MIN'])
#
#output_data = pd.read_csv('O:\\git\\insideTrack\\data\\itineraries1.csv' ,na_filter=False,
#                      usecols=['INCLUDE'])

#all the rows of dataset and dont take last column which is output/ independant variables
#input_data = dataset.iloc[:, :-1].values
# all the rows and take only last column that is output column / dependant variables
#output_data = dataset.iloc[:, 5].values
#0 th index column input data


categorical_columns = ['ORAC1','ORAC2','ORAC3']
numerical_columns = ['NUM_CONNECTIONS','TOTAL_DUR_MIN']
column_trans = make_column_transformer((categorical_columns, OneHotEncoder(handle_unknown='ignore'), (numerical_columns, StandardScaler())))
input_data = column_trans.fit_transform(dataset).toarray()
#Avoiding dummy variable trap
input_data = input_data[:, 1:]
#labelEncoder_orac1 = LabelEncoder()
#input_data[:,0:2] = labelEncoder_orac1.fit_transform(input_data[:,0:2])
#labelEncoder_orac2 = LabelEncoder()
#input_data[:,1] = labelEncoder_orac2.fit_transform(input_data[:,1])
#labelEncoder_orac3 = LabelEncoder()
#input_data[:,2] = labelEncoder_orac3.fit_transform(input_data[:,2])
#
#orac1HotEncoder = OneHotEncoder(categorical_features=[0])
#input_data = orac1HotEncoder.fit_transform(input_data).toarray()
#orac2HotEncoder = OneHotEncoder(categorical_features=[1])
#input_data = orac2HotEncoder.fit_transform(input_data).toarray()
#orac3HotEncoder = OneHotEncoder(categorical_features=[2])
#input_data = orac3HotEncoder.fit_transform(input_data).toarray()

#output data encoding/ dependant variables

labelEncoder_isSelected = LabelEncoder()
output_data = labelEncoder_isSelected.fit_transform(output_data)
output_data= output_data.reshape(-1,1)
# Splitting the dataset into the Training set and Test set
input_data_train, input_data_test, output_data_train, output_data_test = train_test_split(input_data, output_data, test_size = 0.2, random_state = 0)


# Feature Scaling
sc_X = StandardScaler()
input_data_train = sc_X.fit_transform(input_data_train)
input_data_test = sc_X.transform(input_data_test)
sc_y = StandardScaler()
output_data_train = sc_y.fit_transform(output_data_train)
output_data_test = sc_y.fit_transform(output_data_test)

#Fitting multiple linear regression to the training set
from sklearn.linear_model import LinearRegression
regressor = LinearRegression()
regressor.fit(input_data_train,output_data_train)

#predicting the test set results
output_data_pred = regressor.predict(input_data_test)

df_pred = pd.DataFrame.from_dict(output_data_pred)
df_test = pd.DataFrame.from_dict(output_data_test)

df_pred.to_csv('//mnt//non-ssd//hackathon//df_pred.csv')
df_test.to_csv('//mnt//non-ssd//hackathon//df_test.csv')


#polynomial regression
#from sklearn.preprocessing import PolynomialFeatures
#poly_reg = PolynomialFeatures(degree = 2)
#input_poly = poly_reg.fit_transform(input_data_train,output_data_test)
#lin_reg_2 = LinearRegression()
#lin_reg_2.fit(input_poly,output_data_train)
#
#
#output_data_pred_poly = lin_reg_2.predict(input_data_test)
