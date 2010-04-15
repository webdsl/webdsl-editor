Spoofax is included at the update site, because all other options don't work properly;

* actually include spoofax in the feature: creates a lock-in dependency, making it impossible to install any other version of Spoofax
* referencing the actual Spoofax update site: 
	* via feature.xml / <discovery>: no effect
	* via site.xml / associateSites: only for (deprecated) update manager, does not work
		* in p2 compatibility mode, reference update site is added as disabled site

More trouble:

* site.xml builder often screws up; check the generated content.jar by hand or remove it (reverting back to an old-fashion non-p2 update site)


