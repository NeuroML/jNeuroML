"""Script to handle the NeuroML 2 repos"""

import os
import sys
import os.path as op
import subprocess
import urllib
import zipfile
from subprocess import call

def main():
    """Main"""
    mode = "update"
    switch_to_branch = None
    
    if len(sys.argv) < 5:
        for arg in sys.argv[1:]:
            if arg == "clean":
                print("Cleaning repos")
                mode = "clean"
            elif arg == "development":
                switch_to_branch = "development"
            elif arg == "experimental":
                switch_to_branch = "experimental"
            elif arg == "master":
                switch_to_branch = "master"
            else:
                help_info()
                exit()
    else:
        help_info()
        exit()

    neuroml2_spec_repo = ['NeuroML/NeuroML2']
    libneuroml_repo = ['NeuralEnsemble/libNeuroML']

    java_neuroml_repos = ['NeuroML/org.neuroml.model.injectingplugin',
                          'NeuroML/org.neuroml.model',
                          'NeuroML/org.neuroml1.model',
                          'NeuroML/org.neuroml.export',
                          'NeuroML/org.neuroml.import',
                          'NeuroML/jNeuroML']

    neuroml_repos = neuroml2_spec_repo + libneuroml_repo + java_neuroml_repos

    jlems_repo = ['LEMS/jLEMS']
    lems_spec_repos = ['LEMS/LEMS']
    pylems_repos = ['LEMS/pylems']

    java_repos = jlems_repo + java_neuroml_repos
    lems_repos = jlems_repo + lems_spec_repos + pylems_repos

    # Which repos use a development branch?
    dev_branch_repos = neuroml2_spec_repo + neuroml_repos + jlems_repo


    all_repos = lems_repos  + neuroml_repos 
    

    # Set the preferred method for cloning from GitHub
    github_pref = "HTTP"
    # github_pref = "SSH"
    # github_pref = "Git Read-Only"

    pre_gh = {}
    pre_gh["HTTP"] = "https://github.com/"
    pre_gh["SSH"] = "git@github.com:"
    pre_gh["Git Read-Only"] = "git://github.com/"

    for repo in all_repos:

        local_dir = ".." + os.sep + repo.split("/")[1]

        if mode is "clean":
            print("------ Cleaning: %s -------"%repo)
            if repo in java_repos:
                command = "mvn clean"
                print("It's a Java repository, so cleaning using Maven...")
                info = execute_command_in_dir(command, local_dir)

        if mode is "update":

            print("------ Updating: %s -------" %repo)

            runMvnInstall = False

            if not op.isdir(local_dir):
                command = "git clone %s%s" % (pre_gh[github_pref], repo)
                print("Creating a new directory: %s by cloning from GitHub" %(local_dir))
                execute_command_in_dir(command, "..")
                
                runMvnInstall = True

            if switch_to_branch:
                if (repo in dev_branch_repos):
                    command = "git checkout %s" %(switch_to_branch)
                    print("Switching to branch: %s" %(switch_to_branch))
                    exit_on_fail = switch_to_branch is not "experimental"
                    execute_command_in_dir(command, local_dir, exit_on_fail)
                    runMvnInstall = True

            info = execute_command_in_dir("git branch", local_dir)
            print(info.strip())

            return_string = execute_command_in_dir("git pull", local_dir)

            runMvnInstall = runMvnInstall \
                or ("Already up-to-date" not in return_string) \
                or not op.isdir(local_dir + os.sep + "target") \
                or ("jNeuroML" in repo)

            if (repo in java_repos or repo in neuroml2_spec_repo) and runMvnInstall:
                command = "mvn install"
                print("It's a Java repository, so installing using Maven...")
                info = execute_command_in_dir(command, local_dir)
                
                #The code below needs a non trivial rewrite due to python3 differences.

                #                
                #if str("BUILD SUCCESS") in info:
                #    print("Successful installation using : %s!" %command)
                #else:
                #    print("Problem installing using : %s!" %command)
                #    print(info)
                #    exit(1)

    if mode is "update":
        print("All repositories successfully updated & Java modules built!")
        print("You should be able to run some examples straight " \
              "away using jnml: ")
        if os.name is not 'nt':
            print("  ./jnml "\
                "-validate ../NeuroML2/examples/NML2_FullNeuroML.nml")
            print("  ./jnml " \
                "../NeuroML2/LEMSexamples/LEMS_NML2_Ex2_Izh.xml")
        else:
            print("  jnml -validate " \
                "..\\NeuroML2\\examples\\NML2_FullNeuroML.nml")
            print("  jnml " \
                "..\\NeuroML2\\LEMSexamples\\LEMS_NML2_Ex2_Izh.xml")
    if mode is "clean":
        print("All repositories successfully cleaned!")




def execute_command_in_dir(command, directory, exit_on_fail=True):
    """Execute a command in specific working directory"""
    if os.name == 'nt':
        directory = os.path.normpath(directory)
    print(">>>  Executing: (%s) in dir: %s" %(command, directory))
    p = subprocess.Popen(command, cwd=directory, shell=True, stdout=subprocess.PIPE)
    return_str = p.communicate()
     
    if p.returncode != 0:                           
        print("Error: %s" %p.returncode)
        if exit_on_fail: 
            exit(p.returncode)
    return return_str[0]


def help_info():
    print("\nUsage:\n\n    python getNeuroML.py\n        " \
    "Pull (or clone) the latest version of all NeuroML 2 repos & " \
    "compile/install with Maven if applicable\n\n" \
    "    python getNeuroML.py clean\n        " 
    "Run 'mvn clean' on all Java repos\n\n" \
    "    python getNeuroML.py master\n       " \
    "Switch all repos to master branch\n\n" \
    "    python getNeuroML.py development\n       " \
        "Switch relevant repos to development branch\n\n")


if __name__ == "__main__":
    main()
