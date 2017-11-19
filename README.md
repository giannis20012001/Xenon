# Xenon
> Xenon's task is to iterate over a give class (Android api) and fetch all of its methods (public & hidden) during runtime (using reflection)

##Android Device Monitor “storage” folder is empty

The folder /storage can be empty because it is missing the proper permissions. 
To solve this, execute the following commands:
    
* First, use these commands to start a shell in the emulator and grant root rights.


    $ adb shell    
    $ su
    
* Secondly, modify the permissions of the folder (and subfolders recursively) "/storage" in order to make them appear in the tool Android Device Monitor.


    $chmod -R 777 /storage

* Finally, use this command to restart adb as root. 


    $ adb root
    
> Be careful as it only works on development builds (typically emulators builds).
    
* Afterwards, the content of the folder "/storage" are viewable and the data located inside can be transferred. 
You can do it in console as well, using adb pull <remote> <locale>, such as:

    
    $ adb pull /storage/emulated/0/Download/ClassComponents.txt .