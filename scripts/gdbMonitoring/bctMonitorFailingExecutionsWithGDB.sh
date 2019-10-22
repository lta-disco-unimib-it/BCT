#!/bin/bash

export BCT_C_MONITORING_STAGE="faulty"

${BCT_C_HOME}/bctMonitorExecutionsWithGDB.sh $@

