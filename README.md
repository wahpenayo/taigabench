# taigabench

Benchmarks for random forest libraries, including 
[taiga](https://github.com/wahpenayo/taiga).

## Hardware

Lenovo P70 and X1 running Windows 10.

## Libraries

### [MiKTEX 2.9](https://miktex.org/)

### [JDK 9.0.1](http://www.oracle.com/technetwork/java/javase/overview/index.html)

#### Windows 10

Oracle installation puts `C:\ProgramData\Oracle\Java\javapath`
on the system path, which makes it hard to control which JDK is used.

The Clojure launcher scripts use `JAVA8` and `JAVA9` environment
variables to get at the desired installation directly.

This PC -> Properties -> Advanced System Settings -> Environment Variables

Define `JAVA9`, for example, `C:\Program Files\Java\jdk-9.0.1`,
and `JAVA8`, if desired. 

**Note:** H2O requires 
[Java 8](http://www.oracle.com/technetwork/java/javase/downloads/index.html)
(as of 2017-11-17). The H2O benchmarks use the `JAVA8` 
environment variable to get at this, so you will need to both
install a Java 8 JDK or JRE and point `JAVA8` at the installation,
something like `C:\Program Files\Java\jdk1.8.0_152`.

#### Linux 
#### OSX

### [Maven 3.5.2](https://maven.apache.org/index.html)

#### Windows 10

This PC -> Properties -> Advanced System Settings -> Environment Variables

Define `MAVEN_HOME`.

Add `%MAVEN_HOME%\bin` to `PATH`.

#### Linux 
#### OSX

### [Clojure 1.9.0](https://clojure.org/)

Automatic via Maven dependencies in `pom.xml`.

### [taiga](https://github.com/wahpenayo/taiga)

Automatic via Maven dependencies in `pom.xml`.

### [R 3.4.2](https://www.r-project.org/)

Download from [CRAN mirror](https://www.python.org/) and install.

Libraries installed with `install.packages()` or manually
thru the GUI:

* data.table 1.10.4-3
* readr 1.1.1
* ROCR 1.0-7
* randomForest 4.6-12
* xgboost 0.6-4
* parallel 3.4.2
* Matrix 1.2-11
* [h2o](http://docs.h2o.ai/h2o/latest-stable/h2o-docs/welcome.html#r-users)
   3.15.0.4103
* ggplot2 2.2.1

**Note:** can use `sessionInfo()` to check the versions of
the loaded packages.

### [Python 3.6.3](https://www.python.org/)

#### Windows 10

[Windows x86-64 web-based installer](https://www.python.org/downloads/release/python-363/)

#### Linux 
#### OSX

### [scikit-learn 0.19.1](http://scikit-learn.org/stable/)

#### [Anaconda 5.0.1](https://www.anaconda.com/download/)

##### Windows 10

[Windows installer Python 3.6 version](https://www.anaconda.com/download/#windows)

Separate Python install probably not necessary.

*No permissions for `C:\ProgramData\Anaconda3`:*
```
conda create -n my_root --clone="C:\ProgramData\Anaconda3"
activate my_root
```

scikit-learn needs [SciPy 1.0.0](http://www.scipy.org/) 
and [NumPy 1.13.3](http://www.numpy.org/),
whach are probably already present in the anaconda distribution.

`conda install numpy  

`conda install scipy`

`conda install scikit-learn`

##### Linux 
##### OSX

### [Spark mlib](https://spark.apache.org/mllib/)

Installation and version complexity off-putting.
Not benchmarking distributed implmentations (yet) anyway.

## Benchmarks

### classify (Binary classification)

#### [Airline ontime data](http://stat-computing.org/dataexpo/2009)

<table width="100%">
<tbody><tr>
  <th></th>
  <th>Name</th>
  <th>Description</th>
</tr>
<tr>
 <td>1  </td><td> Year              </td><td>1987-2008</td>
</tr><tr>
 <td>2  </td><td> Month             </td><td>1-12</td>
</tr><tr>
 <td>3  </td><td> DayofMonth        </td><td>1-31</td>
</tr><tr>
 <td>4  </td><td> DayOfWeek         </td><td>1 (Monday) - 7 (Sunday)</td>
</tr><tr>
 <td>5  </td><td> DepTime           </td><td>actual departure time (local, hhmm)</td>
</tr><tr>
 <td>6  </td><td> CRSDepTime        </td><td>scheduled departure time (local, hhmm)</td>
</tr><tr>
 <td>7  </td><td> ArrTime           </td><td>actual arrival time (local, hhmm)</td>
</tr><tr>
 <td>8  </td><td> CRSArrTime        </td><td>scheduled arrival time (local, hhmm)</td>
</tr><tr>
 <td>9  </td><td> UniqueCarrier     </td><td><a href="supplemental-data.html">unique carrier code</a></td>
</tr><tr>
 <td>10 </td><td> FlightNum         </td><td>flight number</td>
</tr><tr>
 <td>11 </td><td> TailNum           </td><td>plane tail number</td>
</tr><tr>
 <td>12 </td><td> ActualElapsedTime </td><td>in minutes</td>
</tr><tr>
 <td>13 </td><td> CRSElapsedTime    </td><td>in minutes</td>
</tr><tr>
 <td>14 </td><td> AirTime           </td><td>in minutes</td>
</tr><tr>
 <td>15 </td><td> ArrDelay          </td><td>arrival delay, in minutes</td>
</tr><tr>
 <td>16 </td><td> DepDelay          </td><td>departure delay, in minutes</td>
</tr><tr>
 <td>17 </td><td> Origin            </td><td>origin <a href="supplemental-data.html">IATA airport code</a></td>
</tr><tr>
 <td>18 </td><td> Dest              </td><td>destination <a href="supplemental-data.html">IATA airport code</a></td>
</tr><tr>
 <td>19 </td><td> Distance          </td><td>in miles</td>
</tr><tr>
 <td>20 </td><td> TaxiIn            </td><td>taxi in time, in minutes</td>
</tr><tr>
 <td>21 </td><td> TaxiOut           </td><td>taxi out time in minutes</td>
</tr><tr>
 <td>22 </td><td> Cancelled           </td><td>was the flight cancelled?</td>
</tr><tr>
 <td>23 </td><td> CancellationCode  </td><td>reason for cancellation (A = carrier, B = weather, C = NAS, D = security)</td>
</tr><tr>
 <td>24 </td><td> Diverted          </td><td>1 = yes, 0 = no</td>
</tr><tr>
 <td>25 </td><td> CarrierDelay      </td><td>in minutes</td>
</tr><tr>
 <td>26 </td><td> WeatherDelay      </td><td>in minutes</td>
</tr><tr>
 <td>27 </td><td> NASDelay          </td><td>in minutes</td>
</tr><tr>
 <td>28 </td><td> SecurityDelay     </td><td>in minutes</td>
</tr><tr>
 <td>29 </td><td> LateAircraftDelay </td><td>in minutes</td>
</tr>
</tbody></table>

See the 
[original data source](https://www.transtats.bts.gov/Fields.asp?Table_ID=236)
for more details and 
[data](https://www.transtats.bts.gov/OT_Delay/OT_DelayCause1.asp)
 for the years after 2009.

Benchmark algorithms attempting to predict whether a flight's
departure delay is at least 15 minutes, that is,
binary classification, as in
[BENCHMARKING RANDOM FOREST IMPLEMENTATIONS](http://datascience.la/benchmarking-random-forest-implementations/).
See also the [R-bloggers version](https://www.r-bloggers.com/benchmarking-random-forest-implementations/)
and the [original code](https://github.com/szilard/benchm-ml).

_(I don't know why this uses departure delay as the outcome ---
most passengers would be more interested in arrival delay.
Also, only a subset of the predictors are used:
`month`, `dayofmonth`, `dayofweek`, `deptime`,
`uniquecarrier`, `origin`, `dest`, `distance`.
Our regression versions of this benchmark use arrival delay
and more predictors.)

Download at least 2005, 2006, and 2007 by hand.
Use `src/scripts/r/ontime/classification-data.r` 
to sample and split into `train`, `test`, and `valid` sets,
as was done for 

**Note:** `classification-data.r` randomly splits 2007 into 
`test` and `valid`
sets. A better approach would use 2007 for `test` 
(actually something like meta-train), and 2008 for `valid`,
or split the training data into `pre-train`, `meta-train`,
and are-combine those and re-train once the meta/hyper parameters
have been chosen.

