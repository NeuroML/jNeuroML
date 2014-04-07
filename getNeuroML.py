"""Script to handle the NeuroML 2 repos"""

import os
import sys
import os.path as op
import subprocess


def main():
    """Main"""
    mode = "update"
    switch_to_branch = None

    if len(sys.argv) == 2:
        if sys.argv[1] == "clean":
            print "Cleaning repos"
            mode = "clean"
        elif sys.argv[1] == "development":
            switch_to_branch = "development"
        elif sys.argv[1] == "master":
            switch_to_branch = "master"
        else:
            print "\nUsage:\n\n    python getNeuroML.py\n        " \
                "Pull (or clone) the latest version of all NeuroML 2 repos & " \
                "compile/install with Maven if applicable\n\n" \
                "    python getNeuroML.py clean\n        " \
                "Run 'mvn clean' on all Java repos\n\n" \
                "    python getNeuroML.py master\n       " \
                "Switch all repos to master branch\n\n" \
                "    python getNeuroML.py development\n       " \
                "Switch relevant repos to development branch\n"
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
    dev_branch_repos = neuroml_repos

    all_repos = lems_repos + neuroml_repos

    # Set the preferred method for cloning from GitHub
    github_pref = "HTTP"
    # github_pref = "SSH"
    # github_pref = "Git Read-Only"

    pre_gh = {}
    pre_gh["HTTP"] = "https://github.com/"
    pre_gh["SSH"] = "git@github.com:"
    pre_gh["Git Read-Only"] = "git://github.com/"

    for repo in all_repos:

        local_dir = ".."+os.sep+repo.split("/")[1]

        if mode is "clean":
            print "------ Cleaning: %s -------" % repo
            if repo in java_repos:
                command = "mvn clean"
                print "It's a Java repository, so cleaning using Maven..."
                info = execute_command_in_dir(command, local_dir)

        if mode is "update":

            print
            print "------ Updating: %s -------" % repo

            runMvnInstall = False

            if not op.isdir(local_dir):
                command = "git clone %s%s" % (pre_gh[github_pref], repo)
                print "Creating a new directory: %s by cloning from GitHub" % \
                    (local_dir)
                execute_command_in_dir(command, "..")
                runMvnInstall = True

            if switch_to_branch:
                if (switch_to_branch is not "development") \
                        or (repo in dev_branch_repos):
                    command = "git checkout %s" % (switch_to_branch)
                    print "Switching to branch: %s" % (switch_to_branch)
                    execute_command_in_dir(command, local_dir)
                    runMvnInstall = True

            info = execute_command_in_dir("git branch", local_dir)
            print info.strip()

            return_string = execute_command_in_dir("git pull", local_dir)

            runMvnInstall = runMvnInstall \
                or ("Already up-to-date" not in return_string) \
                or not op.isdir(local_dir+os.sep+"target") \
                or ("jNeuroML" in repo)

            if repo in java_repos and runMvnInstall:
                command = "mvn install"
                print "It's a Java repository, so installing using Maven..."
                info = execute_command_in_dir(command, local_dir)
                if "BUILD SUCCESS" in info:
                    print "Successful installation using : %s!" % command
                else:
                    print "Problem installing using : %s!" % command
                    print info
                    exit(1)

    if mode is "update":
        print
        print "All repositories successfully updated & Java modules built!"
        print
        print "You should be able to run some examples straight " \
              "away using jnml: "
        print
        if os.name is not 'nt':
            print "  ./jnml "\
                "-validate ../NeuroML2/examples/NML2_FullNeuroML.nml"
            print
            print "  ./jnml " \
                "../NeuroML2/NeuroML2CoreTypes/LEMS_NML2_Ex8_AdEx.xml"
        else:
            print "  jnml -validate " \
                "..\\NeuroML2\\examples\\NML2_FullNeuroML.nml"
            print
            print "  jnml " \
                "..\\NeuroML2\\NeuroML2CoreTypes\\LEMS_NML2_Ex8_AdEx.xml"
        print

    if mode is "clean":
        print
        print "All repositories successfully cleaned!"
        print


def execute_command_in_dir(command, directory):
    """Execute a command in specific working directory"""
    if os.name == 'nt':
        directory = os.path.normpath(directory)
    print ">>>  Executing: (%s) in dir: %s" % (command, directory)
    return_string = subprocess.Popen(command, cwd=directory, shell=True,
                                     stdout=subprocess.PIPE).communicate()[0]
    return return_string


if __name__ == "__main__":
    main()
