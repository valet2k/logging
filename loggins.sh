# Brandon Sizemore
# Prototype preexec hook function for logging commands to a separate file

# Add hooking code
autoload -U add-zsh-hook

# Start derby
source load.zsh &> /dev/null

#iset +m
derby &> /dev/null
sleep 1
echo "Welcome to Valet 2000"

#echo "Loading command database..."
#echo "Valet is online"

# Create the variables for temporary storage
CMDS=""
ARGS=""
FILES=""
ENTRY=""
TYPE=""

# Disable monitoring with set +m (prevents background tasks from interuptting constantly)
set +m

# THE PRECMD FUNCTIONS
# Retrieve the latest return value, and log the data to the database.
function status()
{
	ENTRY="$CMDS$ARGS$FILES $?"
                
	./logcmd.sh $ENTRY &> /dev/null &

	# Zero out the variables before the next command is entered
	CMDS=""
	ARGS=""
	FILES=""
	ENTRY=""
	TYPE=""
}


# THE PREEXEC FUNCTIONS
# Log the data into files
function loggins()
{
	# First, try to find the type of command
	command=$1
	CMDS=$command[(w)1]
	
	word_count="$(wc -w <<< "$command")"
	index=1

	while [ $index -le $word_count ]
	do
		word=$command[(w)$index]
		if [ $word = $CMDS ]; then
			index=$(($index+1))
			continue

		elif [[ $word == *-* ]]; then
			ARGS="$ARGS $word"

		elif [ $word = "|" ]; then
			echo "I have a pipe!"

		else
			FILES="$FILES $word"

		fi

		index=$(($index+1))
	done
	
	# Use where to determine whether the command is a builtin, an installed program, or and invalid command
	TYPE=$(where $CMDS)
	#(echo $1)  >> log.txt
	#echo "${@: -1}"
	#echo "${BASH_ARGV[0]}"
	#echo $@
	#echo $#
}

# If possible, check to see if the command entered is valid via mankier.com
function explain ()
{
	if (tango=(nc -zw1 google.com 80)) ; then
		(curl -Gs "https://www.mankier.com/api/explain/?cols="$(tput cols) --data-urlencode "q=$1") >> explain.txt &
	fi
}	

# The ZSHEXIT functions
function shutdown()
{
	echo "Shutting down..."
	derby stop &> /dev/null
	set -m
}

# OTHER USEFULE FUNCTIONS
function quit()
{
	echo "Shutting down..."
	derby stop &> /dev/null
	set -m
	add-zsh-hook -D precmd status
	add-zsh-hook -D preexec loggins
	add-zsh-hook -D zshexit shutdown
	source $HOME/.zshrc
}

# ADD THE HOOK FUNCTIONS
add-zsh-hook precmd  status
add-zsh-hook preexec loggins
#add-zsh-hook preexec explain
add-zsh-hook zshexit shutdown
