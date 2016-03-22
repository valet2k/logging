# Brandon Sizemore
# Prototype preexec hook function for logging commands to a separate file

# Add hooking code
autoload -U add-zsh-hook

# Start derby
echo "Welcome to Valet 2000"
echo "Loading command database..."
./derby.sh &> /dev/null
echo "Valet is online"

# Create the variables for temporary storage
CMDS=""
ARGS=""
FILES=""
ENTRY=""

# THE PRECMD FUNCTIONS
# Retrieve the latest return value, and log the data to the database.
function status()
{
	ENTRY="$CMDS$ARGS$FILES $?"
        #echo "$ENTRY"
        
	./logcmd.sh $ENTRY &> /dev/null

	# Zero out the variables before the next command is entered
	CMDS=""
	ARGS=""
	FILES=""
	ENTRY=""
}


# THE PREEXEC FUNCTIONS
# Log the data into files
function loggins()
{
	# Disable monitoring with set +m (prevents background tasks from interuptting constantly)
	#set +m

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

		else
			FILES="$FILES $word"
		fi

		index=$(($index+1))
	done
	
	# Use where to determine whether the command is a builtin, an installed program, or and invalid command
	(where $CMDS) >> where.txt
	(echo $1)  >> log.txt
	#echo "${@: -1}"
	#echo "${BASH_ARGV[0]}"
	#echo $@
	#echo $#
	# ./script.sh $1 &
	# ./spooky.exe $1 &
}

# If possible, check to see if the command entered is valid via mankier.com
function explain ()
{
	if (tango=(nc -zw1 google.com 80)) ; then
		(curl -Gs "https://www.mankier.com/api/explain/?cols="$(tput cols) --data-urlencode "q=$1") >> explain.txt &
	fi
}	


export PATH=$HOME/bin:/usr/local/bin:$PATH
# export MANPATH="/usr/local/man:$MANPATH"

source $ZSH/oh-my-zsh.sh


# You may need to manually set your language environment
# export LANG=en_US.UTF-8

# Preferred editor for local and remote sessions
# if [[ -n $SSH_CONNECTION ]]; then
#   export EDITOR='vim'
# else
#   export EDITOR='mvim'
# fi

# Compilation flags
# export ARCHFLAGS="-arch x86_64"

# ssh
# export SSH_KEY_PATH="~/.ssh/dsa_id"

# Set personal aliases, overriding those provided by oh-my-zsh libs,
# plugins, and themes. Aliases can be placed here, though oh-my-zsh
# users are encouraged to define aliases within the ZSH_CUSTOM folder.
# For a full list of active aliases, run `alias`.
#
# Example aliases
# alias zshconfig="mate ~/.zshrc"
# alias ohmyzsh="mate ~/.oh-my-zsh"


# ADD THE HOOK FUNCTIONS
add-zsh-hook precmd  status
add-zsh-hook preexec loggins
#add-zsh-hook preexec explain
