ifeq ($(shell which pandoc),)
$(error Please install pandoc using "apt-get install pandoc" or "brew install pandoc")
endif
