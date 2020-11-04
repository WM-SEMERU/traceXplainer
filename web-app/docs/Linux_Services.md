# Linux Services 

### What are linux services and why are they useful to us?

Linux services allow us to run programs right when the host computer boots up. Obviously, this is very powerful.
In our case, we use this ability to start the webserver for tminer. It is important to note that Jenkins also
get started this way, but that was done automatically at the download time. 

### How do they Work?

Service control is done using `systemctl`. the three most important functions we can call (all of this is on the 
command line) are `start`, `stop`, and `status`. The function of `start` and `stop` are perhaps obvious, and `status` gives a description
of the service named as well as an overview of it's current point in the lifecycle and a bit of CLI output if
applicable. all of these commands take the form `(sudo) systemctl <command> <service>`.

### How do I make a service?

all services reside in `/etc/systemd/system`. A service anywhere else won't be run. Each service consists of three
main parts:
- `[Unit]` : A description
- `[Service]` : the actual program being run
     - ExecStart= /path/to/ExecutingAgent(e.g. python, bash) /path/to/program/to/run(all paths are absolute)
- `[Install]` : Helper Info
     - WantedBy= multi-user.target <- this is generally what you want. The program will start when boot is completed
     - Alias= allows you to call systemctl start <servicename> isntead of <servicename.service>

### Quirks

In my time working with services, I've noticed that it is possible for a service to run when the machine is not logged
in to any user account. There is something weird that happens where if you try to run a python script directly and you
are not logged in, stuff breaks, especially when you try to access the filesystem within the script. If instead you 
run a bash script, and that bash script runs a python script, things seem to work fine. I have no idea if this is 
intentional, I don't really even know if this is true, as all I have is anecdotal experience. But it is a good idea
to write services to run bash scripts instead of running python scripts directly.

