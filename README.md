# SciToS [![Build Status (master)](https://travis-ci.org/scientific-tool-set/scitos.svg?branch=master)](https://travis-ci.org/scientific-tool-set/scitos)
The Scientific Tool Set is (planned to be) a collection of helpful tools for the scientific context.
By automating (parts of) recurring tasks, SciToS aims to:
- improve the outcome's quality (Validation)
- save valuable time by simplifying the underlying process (Automation)
- enable further processing and security of sensitive data via digital storage (XML file format)

---

## Contributions
As this is a free and open source software, you are welcome to share your ideas about additional features or even whole modules/tools that are in dire need of being created – via the GitHub Issue Tracker.

Or you can just add those features/modules yourself and create a pull request.

Input of any kind is appreciated.

---

### 1. AIS – Autobiographical Interview Scoring
#### 1.1 Background
Autobiographical Interviews (AI) are a method to tease apart the forms of memory that contribute to a participant's description of a remembered past event.
Discrete units of information are parsed and classified as either internal (i.e. episodic) or external (i.e. non-episodic). See for reference: [Aging and autobiographical memory; Levine et al. 2002](http://www.ncbi.nlm.nih.gov/pubmed/12507363).

It is a useful instrument for quantifying personal remote memory retrieval e.g. in younger/older adults or in patients.

#### 1.2 Motivation
The traditional paper-based scoring is prone to errors and requires additional effort for further processing and regarding compliance.

  - *Old Example Workflow:* interview's audio recording > digital transcribed version > printed out > scored by hand > tallied by hand into spreadsheet
  - *Target Workflow:*  interview's audio recording > digital transcribed version > interview copied into tool > scored in tool > tallied automatically > saved with scoring to file and/or results exported to spreadsheet

#### 1.3 SciToS Module
The tool avoids any unnecessary printouts, and allows the easy digital storage and exchange of produced files.
All interviews belonging to one study/project are combined in a single file.

The actual scoring can be done either via mouse, keyboard short cuts, or a combination thereof.
The classifications (i.e. assigned categories) can be independently configured for each project.
The resulting number of assigned detail categories can be exported to an Open Document Standard (ODS) spreadsheet.
The produced files can also be viewed in any modern web browser, and printed from there.
