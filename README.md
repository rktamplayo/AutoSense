# GAS
Granularity-Agnostic Sense Model for Word Sense Induction

This code was used in the experiments of the research paper

**Reinald Kim Amplayo**, Seung-won Hwang, and Min Song. **Granularity-Agnostic Sense Model for Word Sense Induction**. _AAAI_, 2019.

The `src/models` folder contains one Java file containing the GAS class. To use the model, create an object of GAS using the following line:

`GAS gas = new GAS(data, target, numSenses, numTopics, alpha, beta, gamma);`

where
- `data`: is a list of data instances
- `target`: is the target word
- `numSenses`: is the number of senses hyperparameter
- `numTopics`: is the number of topics hyperparameter
- `alpha`: is the Dirichlet prior of the topic distribution (set to 0.1 in the paper)
- `beta`: is the Dirichlet prior of the sense distribution (set to 0.01 in the paper)
- `gamma`: is the Dirichlet prior of the switch distribution (set to 0.3 in the paper)

Then, you would need to run the Gibbs sampler using the following lines of code:

`gas.initialize();` <br />
`gas.estimate(numIters);`

where `numIters` is the number of iterations (set to 2000 in the paper).

To print the results, use the line:

`gas.printSemEval(filename, target);`

To cite the paper/code, please use this BibTex:

```
@inproceedings{amplayo2019granularity,
	Author = {Reinald Kim Amplayo and Seung-won Hwang and Min Song},
	Booktitle = {AAAI},
	Location = {Honolulu, HI},
	Year = {2019},
	Title = {Granularity-Agnostic Sense Model for Word Sense Induction},
}
```

If you have questions, send me an email: reinald.kim at ed dot ac dot uk
