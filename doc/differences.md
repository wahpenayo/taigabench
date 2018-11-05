# differences between taiga and other libraries

## splitting

### optimizing splits on categorical variables

#### taiga 

For score-able cost functions (l2 regression, binary classification),
taiga effectively converts categorical attributes to numerical,
via the score function.

In other cases, taiga does 
<ul> 
<li> all subsets search when the number categories is less than
`taiga.split.object.categorical.heuristic/CUTOFF`.
(<b>TODO:</b> make this an option.)
<li> Bottom-up hierarchical clustering of categories.
Start with each category as a cluster. 
Compute the splitting cost function for the current clusters
and all possible merges of 2 clusters into one.
Merge the two clusters that least damage the splitting cost.
</ul>

More generally, the `split-learner` is a function-valued option,
so the user can substitute whatever splitting rule they like.

#### randomForestSRC

See [Real versus categorical splitting](https://kogalur.github.io/randomForestSRC/theory.html#section3.1))

It appears that randomForestSRC does all subsets search for 32
categories or fewer, and randomized search otherwise.

### termination criteria

#### Taiga 

<b>1:</b> Uses a <i>feasibility test</i> (passed as a function option
`feasible?`) to determines whether a given split 
on a attribute is allowed. The most common case here is to 
require both children of the split contain some minimum number of 
training records (`mincount`).

If there are no feasible splits on any of the `mtry` sampled
attributes, then splitting stops. 

Most implementations of decision trees stop splitting with the 
number of records in a node hits some minimum value.
This means that it's possible to get a tree where every leaf but
1 has only one record, with the remaining leaf having the minimum
record count. 
Such a tree will almost certainly overfit.
In any case, I believe that's not the intent of `mincount`
parameters, which is to ensure that every node has enough records
to guarantee a stable estimate of the node model (usually just a 
constant, like the mean `y`).

<b>TODO:</b> We might want to ensure we aren't missing 
good splits, somehow sampling only attributes that have 
feaisble splits?

<b>2:</b> Nodes that achieve a `maxdepth` are not split.

<b>3:</b> Nodes that have singular response (`y`) values are not 
split, where 'singular' is exact equality for object-valued
responses and difference within `(Math/ulp (double 1.0))` for 
numerical/vector-valued responses. 

<b>TODO:</b> Should this be relative rather than absolute difference?

<b>TODO:</b> Make the singularity test an option?

<b>4:</b> Singular attributes are not considered for splitting.
If all attributes are singular, then splitting stops.

<b>TODO:</b> This interacts with `mtry`. 
Currently, `mtry` predictors are sampled at each node. 
If all are singular, splitting stops.
Probably we should sample only from non-singular attributes,
adjusting mtry for the number of such.

#### randomForestSRC

See [Node depth and node size](https://kogalur.github.io/randomForestSRC/theory.html#section3.1).

<ol>
<li> The current node depth must be less than the maximum node depth allowed.
<li> The current node size must be at least 2 times the node size specified.
<li> The current node must be impure.
</ol>

## missing data

### response

#### taiga

taiga omits records with missing response (`y`) values.


#### randomForestSRC

### attribute in training data

### attribute in test data, or new category in test data