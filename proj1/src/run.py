import argparse, os, signal, subprocess, psutil

parser = argparse.ArgumentParser()

parser.add_argument('-n', dest='n', default=5, type=int)

args = parser.parse_args()

files = {}

def pollForChanges(directory='peer'):
    changed = False
    # print("Looking in directory: " + directory)
    for filename in os.listdir(directory):
        _fn, extension = os.path.splitext(filename)

        if (os.path.isdir(directory + "/" + filename)):   # if other directory
            changed = pollForChanges(directory + "/" + filename)
            continue

        if extension != '.java': continue     # if not .java file

        fstat = os.stat(directory+'/'+filename)
        stamp = fstat.st_mtime
        inode = fstat.st_ino
        # need to check also if a file that existed in the files dict no longer exists
        try:
            if files[inode] != stamp:
                changed = True
                files[inode] = stamp
            
        except KeyError:
            changed = True
            files[inode] = stamp

    return changed

def compile_peers():
    print("Compiling...")
    os.chdir("peer")
    out = subprocess.run(["./compile"], capture_output=True)
    os.chdir("..")
    if (out.returncode == 0):
        print("SUCCESS")
    else:
        print(out.stderr.decode('ASCII'))
    return out.returncode == 0

def run_peer(id):
    os.execvp("java", ["java", "Peer", str(id)])

def start_peers():
    os.chdir("peer")
    processes = []
    for i in range(args.n):
        newpid = os.fork()
        if newpid == 0:
            run_peer(i)
        else:
            processes.append(newpid)
    os.chdir("..")
    print("Created processes: " + str(processes))
    return processes

def close_processes(processes):
    for pid in processes:
        if not psutil.pid_exists(pid):
            print("PID " + str(pid) + " not found.")
            continue
        os.kill(pid, signal.SIGTERM)
        os.wait()


try:
    pollForChanges()
    if not compile_peers(): exit()
    while True:
        processes = start_peers()
        while True:
            input()
            # for k in files.keys():
            #     print(str(k) + ": " + str(files[k]))
            if (pollForChanges()): 
                print("Found Changes")
                if (compile_peers()): # improvement: only compile the files that were changed
                    break
        
        close_processes(processes)
except KeyboardInterrupt:
    print("Exiting gracefully.")
    #close_processes(processes)