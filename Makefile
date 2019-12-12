all:

refresh_all_requirements:
	@echo '' >| requirements.txt
	@if [ $$(uname -s) == "Darwin" ]; then sleep 1; fi  # this is require because Darwin HFS+ only has second-resolution for timestamps.
	@touch requirements.txt.in
	@$(MAKE) requirements.txt

requirements.txt : %.txt : %.txt.in
	[ ! -e .requirements-env ] || exit 1
	virtualenv -p $(shell which python3) .$<-env
	.$<-env/bin/pip install -r $@
	.$<-env/bin/pip install -r $<
	echo "# You should not edit this file directly.  Instead, you should edit $<." >| $@
	.$<-env/bin/pip freeze >> $@
	rm -rf .$<-env
