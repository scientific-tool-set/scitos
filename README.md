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

  - **Old Example Workflow**: interview's audio recording > digital transcribed version > printed out > scored by hand > tallied by hand into spreadsheet
  - **Target Workflow**:  interview's audio recording > digital transcribed version > interview copied into tool > scored in tool > tallied automatically > saved with scoring to file and/or results exported to spreadsheet

#### 1.3 SciToS Module
The tool avoids any unnecessary printouts, and allows the easy digital storage and exchange of produced files.
All interviews belonging to one study/project are combined in a single file.

The actual scoring can be done either via mouse, keyboard short cuts, or a combination thereof.
The classifications (i.e. assigned categories) can be independently configured for each project.
The resulting number of assigned detail categories can be exported to an Open Document Standard (ODS) spreadsheet.
The produced files can also be viewed in any modern web browser, and printed from there.

### 2. HmX - HermeneutiX - Syntactic/Semantic Structure Analysis of complex texts
#### 2.1 Background
Syntactic and semantic structure analyses of complex texts are a way to capture the original authors intentions. This is useful when translating those texts into other languages while maintaining the texts' original content - e.g. when translating bible excerpts.
There are a number of different methods for (biblical) exegesis. HermeneutiX aims at two of them and was inspired by the specific approaches teached by Heinrich von Siebenthal (at the FTH Gießen, Germany).

##### 2.1.1 Syntactic Structure Analysis
An early step in the interpretation of a complex foreign language text is to determine its syntactic structure, i.e. what meaning parts of the text have in the whole context. This is being done by letting the user separate the text in syntactically meaningful segments - namely propositions and clause items - and assigning their respective syntactical functions. The origin language of the translated text determines what kind of syntactic constructs exist.

The identified syntactic structure of the texts can be used as a guideline when creating a first translation/interpretation of each individual proposition.

##### 2.1.2  Semantic Structure Analysis
Another (potential) step in pursuit of a good interpretation/translation is to analyze the semantic relations between the previously identified propositions. What kind of semantic relations are available is up to the analyzing user.

The resulting (tree-like) structure should be preserved in the creation of an enhanced translation.

#### 2.2 Motivation
  - **Simplification/Time Saving** in developing the analyses (compared to the extensive arranging in Office- or drawing programs)
  - **Exchangeability** of the analyses between HermeneutiX users (due to the lightweight XML file format)
  - **Uniformity** of notation enabling the user to focus on contents at hand instead of the cumbersome layouting of a multitude of elements
  - **Usability** of results due to the integrated SVG export (enabling arbitrary image size without any loss of quality)
  - **Teaching and Learning Aid** (adaptability of font size for all texts in the analysis view in favor of projector usage)

#### 2.3 SciToS Module
The HmX module in SciToS is a port of the standalone HermeneutiX application (originally created in 2009 and available via [SourceForge](https://sourceforge.net/projects/hermeneutix)). The main purpose is to guide the user creating the analyses described in the Background section.

The main advantage of the newer/ported version - besides better test coverage and a few bugs being fixed - are the extended configuration options via the graphical user interface:

  - Adding/Changing/Removing supported **origin text languages** (beyond the default: Greek and Hebrew).
  - Adding/Changing/Rearranging/Removing selectable **syntactical functions**.
  - Adding/Changing/Rearranging/Removing selectable **semantical relations**.
  - The applied origin text language (including the associated syntactical functions) is being stored as part of a .hmx save file and therefore portable.
