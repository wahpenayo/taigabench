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
variables to get at the desired ins tallation directly.

This PC -> Properties -> Advanced System Settings -> Environment Variables

Define `JAVA9`, for example, `C:\Program Files\Java\jdk-9.0.1`,
and `JAVA8`, if desired. 

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
 
### [Python 3.6.3](https://www.python.org/)

#### Windows 10

[Windows x86-64 web-based installer](https://www.python.org/downloads/release/python-363/)

#### Linux 
#### OSX

### [Anaconda 5.0.1](https://www.anaconda.com/download/)

#### Windows 10

[Windows installer Python 3.6 version](https://www.anaconda.com/download/#windows)

Separate Python install probably not necessary.

*No permissions for `C:\ProgramData\Anaconda3`:*
```
conda create -n my_root --clone="C:\ProgramData\Anaconda3"
activate my_root
```


#### Linux 
#### OSX

### [SciPy 1.0.0](http://www.scipy.org/) and [NumPy 1.13.3](http://www.numpy.org/)

`conda install numpy  

`conda install scipy`

### [scikit-learn 0.19.1](http://scikit-learn.org/stable/)

`conda install scikit-learn`


## Benchmarks

### [Airline ontime data](http://stat-computing.org/dataexpo/2009)

Download at least 2005, 2006, and 2007 by hand.
Use `src/scripts/r/ontime/classification-data.r` 
to sample and split into `train`, `test`, and `valid` sets,
as was done for 
[BENCHMARKING RANDOM FOREST IMPLEMENTATIONS](http://datascience.la/benchmarking-random-forest-implementations/).
See also the [R-bloggers version](https://www.r-bloggers.com/benchmarking-random-forest-implementations/)
and the [original code](https://github.com/szilard/benchm-ml).


**Note:** `gendata` randomly splits 2007 into `test` and `valid`
sets. A better approach would use 2007 for `test` 
(actually something like meta-train), and 2008 for `valid`,
or split the training data into `pre-train`, `meta-train`,
and are-combine those and re-train once the meta/hyper parameters
have been chosen.



'