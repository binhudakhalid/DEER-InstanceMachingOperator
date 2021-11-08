Questions:

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


Todos:

- [x] Config
  - added parameters for testing (orElse)

- [x] Generate Model output out of instance matching
- [x] write tests for just consolidation with given output 
- [x]  constructModel runnable

- [ ]  export fusion/consolidation and use it as class !
- [x] export as TTL file
- [x] 
- [ ] Timing ~ 36 secs atm
- [ ] Add Endpoints (atm just file) (should be via config)
- [ ] config runnable & maybe control if different plugins are used
  - (output file)
  - (target merge to source)
- [ ] Different inputs (waiting for other groups to provide smth)
  - Endpoints and Data 
- [ ] Property matching (onto)
- [ ] ask kevin how to map in config source - property 