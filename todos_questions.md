Questions:

- Literal only consolidation?
- samePropertyMatching seem to have a mistake as name is not in there?
- 


Todos:

- [ ]  export fusion/consolidation and use it as class !
- [ ] Timing Test
- [x] Add Endpoints (atm just file) (should be via config)
    - List of endings for file read ; else 
- [x] add different Tests with workflow (or add different configs and load them like instance matcher)
    - added config based tests too . without property matching works , else not
- [ ] Add config with Authority Conformation Enrichment Operator 
- [ ] Different inputs (waiting for other groups to provide smth)
  - Endpoints and Data 
- [ ] Property matching (onto)
- [x] Data Write back
  - source is writeback
- [ ] Provenance - Prove0
- [x] PropertyMatching same Name
- 

oldTodos

- [x] Config
  - added parameters for testing (orElse)
- [x] Generate Model output out of instance matching
- [x] write tests for just consolidation with given output
- [x]  constructModel runnable
- [x] export as TTL file
- [x] ask kevin how to map in config source - property
- [x] safeApply
- [x] User Able to choose fusionstrategy by sourceproperty
- [x] Before Setup of common testings


oldQuestions
- [ ] two different forms of output
  - to feed authority conform operator
- [ ] Data Write Back
  - source is the only interesting part?
- [ ] Provenance
  - Structure?
  - Where & when
  - where can it be hanged
  - take provenance from targetDataSet ?!
- Reification [Link](https://www.w3.org/wiki/RdfReification) to make statements about statements
- [ProveO](https://www.w3.org/TR/prov-o/)
- [ ] Property Matching
  - there is no property matching yet, so I took the namne from the previous group and inserted that .
    the given Dataset hadnt any others for fusion
- [ ] Interface / Abstract Class
  - it works for my case now, but its all in one class, I need a parentclass in any sort that has the ability to use the
    dispatch map (static & abstract ?!)
- [ ] How to use the config reasonably
  - Doc isn't precise & idk if i can do control sequences.
  - e.g  Authority Conformation Enrichment Operator only if takeTarget
  - or do i have two configs
  - 