# -*- coding: utf-8 -*-
"""
Created on Fri Nov  7 16:47:05 2018

@author: atp1ksb
"""



# Importing the libraries
import numpy as np
import matplotlib.pyplot as plt
import pandas as pd
import glob
import time
#from neo4j.v1 import GraphDatabase
from collections import OrderedDict
from datetime import datetime
import csv


# nrows=1000,
#dataset = pd.read_csv('C:\\Hackathon\\T090418.AF.CSV' , nrows=5, 
#                      usecols=['ORG_CTY1', 'ORG_CTY2', 'ORG_CTY3' ,'ORG_CTY4', 'ORG_CTY4' ,'ORG_CTY5' ,'ORG_CTY6', 'ORG_CTY7',
#                               'DST_CTY1','DST_CTY2','DST_CTY3','DST_CTY4','DST_CTY5','DST_CTY6','DST_CTY7'])
#airportData.dropna(subset=['iata_code'], inplace=True)
 #neo4jDriver = GraphDatabase.driver('http://rndrpdb3:7301', auth=('', ''))
l = [pd.read_csv(filename , memory_map =True,
                 usecols=['ORAC1', 'ORAC2', 'ORAC3' ,'ORAC4', 'ORAC4' ,'ORAC5' ,'ORAC6', 'ORAC7','ORAC8',
                          'DSTC1','DSTC2','DSTC3','DSTC4','DSTC5','DSTC6','DSTC7','DSTC8',
                          'FLT_DATE1','FLT_DATE2','FLT_DATE3','FLT_DATE4','FLT_DATE5','FLT_DATE6','FLT_DATE7','FLT_DATE8',
                          'DPTR_TM1' , 'DPTR_TM2' , 'DPTR_TM3' ,'DPTR_TM4','DPTR_TM5','DPTR_TM6','DPTR_TM7','DPTR_TM8',
                          'ARRV_TM1','ARRV_TM2','ARRV_TM3','ARRV_TM4','ARRV_TM5','ARRV_TM6','ARRV_TM7','ARRV_TM8',
                          'MCAR1','MCAR2','MCAR3','MCAR4','MCAR5','MCAR6','MCAR7','MCAR8','MFTN1','MFTN2','MFTN3','MFTN4','MFTN5','MFTN6','MFTN7','MFTN8']) 
    for filename in glob.glob("//mnt//non-ssd//AirFrance//*.CSV")]
dataset = pd.concat(l, axis=0)

#dataset = pd.read_csv('C:\\Hackathon\\T090418.AF.CSV' , memory_map =True,
#                      usecols=['ORAC1', 'ORAC2', 'ORAC3' ,'ORAC4', 'ORAC4' ,'ORAC5' ,'ORAC6', 'ORAC7',
#                               'DSTC1','DSTC2','DSTC3','DSTC4','DSTC5','DSTC6','DSTC7'])
#NL_FR 751 / US_FR 1304 / FR 730
airportData = pd.read_csv('//mnt//non-ssd//hackathon//airports.csv', memory_map =True, usecols=['iso_country' , 'iata_code'])
airportData = airportData.loc[airportData['iso_country'].isin(['FR','US'])]

fr_us_only=dataset
#fr_us_only = dataset[dataset['ORAC1'].isin(airportData['iata_code'])]
#fr_us_only = fr_us_only[fr_us_only['ORAC2'].isin(airportData['iata_code'])]
#fr_us_only = fr_us_only[fr_us_only['ORAC3'].isin(airportData['iata_code'])]
#fr_us_only = fr_us_only[fr_us_only['ORAC4'].isin(airportData['iata_code'])]
""""fr_us_only = fr_us_only[fr_us_only['ORAC5'].isin(airportData['iata_code'])]
fr_us_only = fr_us_only[fr_us_only['ORAC6'].isin(airportData['iata_code'])]
fr_us_only = fr_us_only[fr_us_only['ORAC7'].isin(airportData['iata_code'])]
fr_us_only = fr_us_only[fr_us_only['ORAC8'].isin(airportData['iata_code'])]

fr_us_only = fr_us_only[fr_us_only['DSTC1'].isin(airportData['iata_code'])]
fr_us_only = fr_us_only[fr_us_only['DSTC2'].isin(airportData['iata_code'])]
fr_us_only = fr_us_only[fr_us_only['DSTC3'].isin(airportData['iata_code'])]
fr_us_only = fr_us_only[fr_us_only['DSTC4'].isin(airportData['iata_code'])]
fr_us_only = fr_us_only[fr_us_only['DSTC5'].isin(airportData['iata_code'])]
fr_us_only = fr_us_only[fr_us_only['DSTC6'].isin(airportData['iata_code'])]
fr_us_only = fr_us_only[fr_us_only['DSTC7'].isin(airportData['iata_code'])]
fr_us_only = fr_us_only[fr_us_only['DSTC8'].isin(airportData['iata_code'])]
"""

#ORG_ DST_ keep a set of objects containing city and count
itenary = {}
for idx, row  in fr_us_only.iterrows():
    matchFound = False 
    trip1String=""
    trip2String=""
    trip1 =OrderedDict([])
    trip2 =OrderedDict([])
    daysDiff , cnt1 , cnt2 =0 ,0,0
    for x in range(1, 8):
           #if(pd.isnull(row['ORAC' +str(x)])) : 
               #print (abs(datetime.strptime(row['FLT_DATE' +str(x)], '%Y-%m-%d') - datetime.strptime(row['FLT_DATE' +str(x+1)], '%Y-%m-%d')) )
           d1 = str(row['FLT_DATE' +str(x)])
           d2 = str(row['FLT_DATE' +str(x+1)])
           if(str(d2) == 'nan' and str(d1) != 'nan'):
               row['FLT_DATE' +str(x+1)] = row['FLT_DATE' +str(x)]
           if(str(d1) != 'nan' and str(d2) != 'nan'):
               daysDiff = (datetime.strptime(d2, '%Y-%m-%d') - datetime.strptime(d1, '%Y-%m-%d')).days
           if(daysDiff >1) :
               matchFound = True
           else :
               if(matchFound == False) :
                   if( str(row['ORAC' +str(x)]) != 'nan'):
                       trip1[row['ORAC' +str(x)]] = (row['ORAC' +str(x)])
                   if(str(row['DSTC' +str(x)]) != 'nan'):
                       trip1[row['DSTC' +str(x)]] = (row['DSTC' +str(x)])
               else: 
                   if( str(row['ORAC' +str(x)]) != 'nan'):
                       trip2[row['ORAC' +str(x)]] = (row['ORAC' +str(x)])
                   if(str(row['DSTC' +str(x)]) != 'nan'):
                       trip2[row['DSTC' +str(x)]] = (row['DSTC' +str(x)])

    for key in trip1.keys():trip1String +="'" +(trip1[key]) + "',"
    for key in trip2.keys():trip2String +="'" +(trip2[key]) + "',"
    
    itenary[trip1String] = itenary.get(trip1String, 0) + 1
    itenary[trip2String] = itenary.get(trip2String, 0) + 1


fr_us_only.to_csv('//mnt//non-ssd//hackathon//fr_us_only2.csv')

with open('//mnt//non-ssd//hackathon//itenary2.csv','w') as f:
    w = csv.writer(f)
    w.writerows(itenary.items())


sorted_by_value = sorted(itenary.items(), key=lambda kv: kv[1] ,reverse = True)
with open('//mnt//non-ssd//hackathon//sorted_itenary.csv','w') as f:
    w = csv.writer(f)
    w.writerows(sorted_by_value.items())



