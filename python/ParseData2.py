# -*- coding: utf-8 -*-
"""
Created on Fri Nov  2 16:47:05 2018

@author: atp1ksb
"""



# Importing the libraries
#import numpy as np
#import matplotlib.pyplot as plt
import pandas as pd
import glob
#import time
#from neo4j.v1 import GraphDatabase
from collections import OrderedDict
from datetime import datetime
#import csv


def setup_o_and_d(row) :
    row['DPTR_TM'] = [""]
    row['ARRV_TM'] = [""]
    row['FLT_DATE'] = [""]
    row['ORAC'] = [""]
    row['DSTC'] = [""]
    row['MCAR'] = [""]
    row['MFTN'] = [""]

def add_to_string(source_row, index, col_name, dest_row):
    if(source_row[col_name + str(index)] != 'nan'):
        string = dest_row[col_name][0]
        if string: string += ';'
        string += str(source_row[col_name + str(index)])
        dest_row[col_name][0] = string


def add_to_strings(source_row, index, dest_row):
    add_to_string(source_row, index, "DPTR_TM", dest_row)
    add_to_string(source_row, index, "ARRV_TM", dest_row)
    add_to_string(source_row, index, "FLT_DATE", dest_row)
    add_to_string(source_row, index, "ORAC", dest_row)
    add_to_string(source_row, index, "DSTC", dest_row)
    add_to_string(source_row, index, "MCAR", dest_row)
    add_to_string(source_row, index, "MFTN", dest_row)


def add_flt_path(row):
    row['FLT_PATH'] = row['ORAC'][0] + ';' + row['DSTC'][0].split(';')[-1]
    
l = [pd.read_csv(filename , memory_map =True, nrows = 500,
                 usecols=['ORAC1', 'ORAC2', 'ORAC3' ,'ORAC4', 'ORAC4' ,'ORAC5' ,'ORAC6', 'ORAC7','ORAC8',
                          'DSTC1','DSTC2','DSTC3','DSTC4','DSTC5','DSTC6','DSTC7','DSTC8',
                          'FLT_DATE1','FLT_DATE2','FLT_DATE3','FLT_DATE4','FLT_DATE5','FLT_DATE6','FLT_DATE7','FLT_DATE8',
                          'DPTR_TM1' , 'DPTR_TM2' , 'DPTR_TM3' ,'DPTR_TM4','DPTR_TM5','DPTR_TM6','DPTR_TM7','DPTR_TM8',
                          'ARRV_TM1','ARRV_TM2','ARRV_TM3','ARRV_TM4','ARRV_TM5','ARRV_TM6','ARRV_TM7','ARRV_TM8',
                          'MCAR1','MCAR2','MCAR3','MCAR4','MCAR5','MCAR6','MCAR7','MCAR8',
                          'MFTN1','MFTN2','MFTN3','MFTN4','MFTN5','MFTN6','MFTN7','MFTN8','FILE_DATE']) 
    for filename in glob.glob("//mnt//non-ssd//AirFrance//*.CSV")]
dataset = pd.concat(l, axis=0)

fr_us_only = dataset

itinerary = {}
frames = []
for idx, row  in fr_us_only.iterrows():
    row1 = {}
    setup_o_and_d(row1)
    
    row1["FILE_DATE"] = [row["FILE_DATE"]]
    
    matchFound = False 
    trip1String=""
    trip2String=""
    trip1indices = ()
    trip2indices = ()
    trip1 =OrderedDict([])
    trip2 =OrderedDict([])
    daysDiff , cnt1 , cnt2 =0 ,0,0
    empty = True
    for x in range(1, 8):
        if(matchFound) :
            add_flt_path(row1)
            frames.append(pd.DataFrame.from_dict(row1))
            row1 = {}
            setup_o_and_d(row1)
            row1["FILE_DATE"] = [row["FILE_DATE"]]
            matchFound = False
            empty = True
        d1 = str(row['FLT_DATE' +str(x)])
        d2 = str(row['FLT_DATE' +str(x+1)])
         
        daysDiff = 0
        if(str(d1) != 'nan' and str(d2) != 'nan'):
            daysDiff = (datetime.strptime(d2, '%Y-%m-%d') - datetime.strptime(d1, '%Y-%m-%d')).days
        if(daysDiff > 1 or (str(d1) == 'nan' and str(d2) != 'nan')) :
            matchFound = True
        if(str(row['DPTR_TM' + str(x)]) == 'nan'): continue;
        else: 
            add_to_strings(row, x, row1)
            empty = False
        


    if(not empty): 
        add_flt_path(row1)
        frames.append(pd.DataFrame.from_dict(row1))
    
output_dataset = pd.concat(frames)

output_dataset.to_csv('//mnt//non-ssd//hackathon//itinerary_test.csv')


