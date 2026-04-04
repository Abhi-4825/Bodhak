# Bodhak – Intelligent Code Dependency & Analysis Tool

<p align="center">
  <b>Understand complex Java codebases visually.</b><br>
  Interactive dependency graphs • Method-level insights • Built for developers
</p>

---

##  Tech Stack

![Java](https://img.shields.io/badge/Java-24-orange)
![JavaFX](https://img.shields.io/badge/UI-JavaFX-blue)
![Status](https://img.shields.io/badge/Status-Active%20Development-yellow)
![Focus](https://img.shields.io/badge/Focus-Developer%20Tooling-green)

---

##  What is Bodhak?

**Bodhak** is a developer tool that helps you **analyze and understand Java codebases** through **interactive dependency graphs**.
> [!TIP]
> **Bodhak** is a Sanskrit word meaning *"one who enlightens"* or *"the indicator."*

It enables you to:
- Visualize relationships between classes and methods  
- Explore how components interact  
- Navigate complex codebases faster  

---

##  Why This Project Matters

Understanding large codebases is one of the biggest challenges developers face.

Bodhak addresses this by:
- Converting static code into **interactive visual graphs**
- Providing **structural clarity** for debugging and refactoring
- Laying the foundation for **AI-assisted code understanding**

---

##  Key Features

### Dependency Visualization
- Class-level and method-level dependency graphs  
- Clearly displays:
  - `Depends On`
  - `Used By`  
- Helps in understanding code structure and relationships  

---

###  Advanced Code Analysis
-  Circular dependency detection  
-  Identification of:
  - God classes (high complexity / too many responsibilities)  
  - Risk-prone classes  
  - Unused classes  

---

###  Intelligent Refactoring Support
- Suggests refactoring strategies for improving code quality  
- Uses **Genetic Algorithm (GA)** concepts for optimization-based improvements  
- Focuses on better modularity and maintainability  

---

###  Interactive Exploration
- Expand and explore class relationships dynamically  
- Navigate through dependencies visually  
- Designed for intuitive developer experience  

---

###  Extensible Architecture
- Modular design (builders, models, graph engine)  
- Easily extendable for:
  - New analysis techniques  
  - AI-powered enhancements  

---

## Requirements

-  **JDK 24 (Required)**
-  JavaFX configured
-  Java project for analysis  

>  May not run on older JDK versions.

---

###  How to Run
##  Installation

1.  **Clone the Repository**
    ```bash
    git clone https://github.com/Abhi-4825/Bodhak.git
    cd bodhak
    ```

2.  **Environment Setup**
    > [!IMPORTANT]
    > This project utilizes modern Java 24 features. Ensure your IDE (IntelliJ IDEA, Eclipse, or VS Code) is configured with the **JDK 24** toolchain.

3.  **Run the Application**
    * Navigate to `src/main/java/Main.java`.
    * Execute the file through your IDE.
  
4. Click **"Select Project"**  
5. Choose your Java project folder  
6. Wait for analysis to complete  
7. Click **"Analyze"**  
8. Click on classes to explore dependencies  

---

##  Known Limitations (Honest Status)

###  Incomplete Exception Handling
- Some edge cases are not handled yet  
- May cause unexpected crashes  

---

###  Dependency Graph Issue
- Root node correctly shows:
  - `Depends On`
  - `Used By`  
- Other nodes may incorrectly show both  

---

###  UI State Issue
- Re-selecting project folder without choosing a new one:
  - May reset project state to `null`  

---

###  Feature Stability
- Some features are partially implemented  
- Method-level updates can be inconsistent  

---

##  Current Status

>  Actively Developing

Focused on:
- Improving accuracy  
- Fixing graph logic  
- Stabilizing UI  

---

##  Future Roadmap

-  AI-powered code summarization  
-  Smart refactoring suggestions  
-  Usage-based dependency insights  
-  Search & filtering  
- Export graphs  

---

##  Ideal Use Cases

- Learning unfamiliar codebases  
- Debugging dependency issues  
- Visualizing architecture  
- Academic and experimental use  

---

##  Developer Notes

This project reflects:
- Strong understanding of **Java + JavaFX**
- Experience with **graph-based modeling**
- Focus on solving **real developer problems**
- Ability to design **scalable and extensible systems**

---

##  Final Thought

Bodhak is a step toward building **intelligent developer tools**  
that make codebases easier to understand, navigate, and improve.

---
