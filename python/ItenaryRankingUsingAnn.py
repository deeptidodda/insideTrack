# -*- coding: utf-8 -*-
"""
Created on Wed Jan 16 16:04:54 2019

@author: atp1ksb
"""

# Artificial Neural Network

# Installing Theano
# pip install --upgrade --no-deps git+git://github.com/Theano/Theano.git

# Installing Tensorflow
# Install Tensorflow from the website: https://www.tensorflow.org/versions/r0.12/get_started/os_setup.html

# Installing Keras
# pip install --upgrade keras

# Part 1 - Data Preprocessing

# Importing the libraries
import numpy as np
import matplotlib.pyplot as plt
import pandas as pd
import glob
from datetime import datetime

# Importing the dataset
#dataset = pd.read_csv('C:\\Users\\ATP1KSB\\Documents\\Tasks\\Hackathon\\BCN-LAX-AA2\\BCN-LAX-20190121.csv')
#dtypes = {'DPTR_TM1':np.datetime64,'ARRV_TM1': np.datetime64, 'NUM_CONNECTIONS':np.int64, 'TOTAL_DUR_MIN':np.int64 ,'INCLUDE':np.bool}
#parse_dates=[[2,1], [2,0]],
l = [pd.read_csv(filename , memory_map =True,na_filter=False, 
                 usecols=['DPTR_TM1','ARRV_TM1','FLT_DATE1' ,'NUM_CONNECTIONS','TOTAL_DUR_MIN','TOTAL_CONNECTION_TIME_MIN', 'MAX_CONNECTION_TIME_MINUTES', 'DEPARTURE_DOW','ARRIVAL_DOW', 'RELATIVE_DURATION', 'INCLUDE']) 
    for filename in glob.glob("C:\\Users\\ATP1KSB\\Documents\\Tasks\\Hackathon\\BCN LAX ITINERARIES MARKED\\*.csv")]
dataset = pd.concat(l, axis=0)

dataset['FLT_DATE1'] = dataset['FLT_DATE1'].apply(lambda date:  datetime.strptime(date,"%Y-%m-%d").timetuple().tm_yday)
temp = dataset['DPTR_TM1'].str.split(":", n = 1, expand = True) 
dataset['DPTR_TM1'] = temp.apply(lambda x: (int(x[0]) *60 + int(x[1]) ) *60 , axis =1 )

temp = dataset['ARRV_TM1'].str.split(":", n = 1, expand = True) 
dataset['ARRV_TM1'] = temp.apply(lambda x: (int(x[0]) *60 + int(x[1]) ) *60 , axis =1 )

#dataset['DPTR_TM1'] = pd.to_datetime(dataset['DPTR_TM1'] ,format='%H:%M').astype(np.int64)
#dataset['FLT_DATE1_DPTR_TM1'] = pd.to_datetime(dataset['FLT_DATE1_DPTR_TM1']).astype(np.int64)
#dataset = dataset.reset_index(drop=True)
#dataset[8] = (dataset[4]  < 1500)

X = dataset.iloc[:, 0:10].values
y = dataset.iloc[:, 10:11].values

# Encoding categorical data
# from sklearn.preprocessing import LabelEncoder, OneHotEncoder
# labelencoder_X_1 = LabelEncoder()
# X[:, 1] = labelencoder_X_1.fit_transform(X[:, 1])
# labelencoder_X_2 = LabelEncoder()
# X[:, 2] = labelencoder_X_2.fit_transform(X[:, 2])
# onehotencoder = OneHotEncoder(categorical_features = [1])
# X = onehotencoder.fit_transform(X).toarray()
# X = X[:, 1:]

# Splitting the dataset into the Training set and Test set
from sklearn.model_selection import train_test_split
X_train, X_test, y_train, y_test = train_test_split(X, y, test_size = 0.2, random_state = 0)

# Feature Scaling
from sklearn.preprocessing import StandardScaler
sc = StandardScaler()
X_train = sc.fit_transform(X_train)
X_test = sc.transform(X_test)

# Part 2 - Now let's make the ANN!

# Importing the Keras libraries and packages
import keras
from keras.models import Sequential
from keras.layers import Dense

# Initialising the ANN
classifier = Sequential()

# Adding the input layer and the first hidden layer
classifier.add(Dense(output_dim = 5, init = 'uniform', activation = 'relu', input_dim = 10))

# Adding the second hidden layer
classifier.add(Dense(output_dim = 5, init = 'uniform', activation = 'relu'))

# Adding the second hidden layer
classifier.add(Dense(output_dim = 4, init = 'uniform', activation = 'sigmoid'))
# Adding the output layer
classifier.add(Dense(output_dim = 1, init = 'uniform', activation = 'sigmoid'))

# Compiling the ANN
classifier.compile(optimizer = 'adam', loss = 'binary_crossentropy', metrics = ['accuracy'])

# Fitting the ANN to the Training set
classifier.fit(X_train, y_train, batch_size = 10, nb_epoch = 10)

# Part 3 - Making the predictions and evaluating the model

# Predicting the Test set results
y_pred = classifier.predict(X_test)
y_pred = (y_pred > 0.0034)
y_pred1 = (y_pred > 0.5)
#y_pred = (y_pred > 0.0034)
#print(dataset.groupby('INCLUDE').count())
print(np.unique(y_test, return_counts=True))
print(np.unique(y_pred, return_counts=True))
print(np.unique(y_pred1, return_counts=True))

# Making the Confusion Matrix
from sklearn.metrics import confusion_matrix
cm = confusion_matrix(y_test, y_pred1)

scores = classifier.evaluate(X_train, y_train)
print("\n%s: %.2f%%" % (classifier.metrics_names[1], scores[1]*100))

#draw ANN diagram in pdf
#from ann_visualizer.visualize import ann_viz;
#ann_viz(classifier, title="Itinerary Ranking Using ANN")
