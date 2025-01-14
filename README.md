﻿
 
[![Android Weekly #498](https://androidweekly.net/issues/issue-498/badge)](https://androidweekly.net/issues/issue-498) [![Android Arsenal](https://img.shields.io/badge/Android%20Arsenal-snippet--timekeeper-brightgreen.svg?style=flat)](https://android-arsenal.com/details/1/8263)

# Snippet  
  
`Snippet` is an extensible android library to measure execution times of the code sections  in a way that does not compromise with the readability and can be shipped to production in a way that the measurement code will be a no-op, without any additional setup. New behaviours can be added in the library by extending Execution paths. 2 execution paths provided with the library are:
 1. `MeasuredExecutionPath` - The code path the does the measurement code spans
 2. `ReleaseExecutionPath` - A no-op path (default path) that is usually installed in the release variants.  

  
# Features  
  
1. Easy to integrate and configure  
2. Switch behavior depending on build type  
3. Reduces boiler plate  
4. Data reuse  
5. Makes PR reviews more quantitative  
6. APK size impact of 23KB  
7. Designed to be thread safe & null safe  
8. Rich API  
9. Fully documented, just run java docs!  
10. Thread safe
  
# Vocabulary  
  
1. <b> Capture </b> : Logical span of code. Can be contiguous or non-contiguous.     
2. <b> Splits </b> : Sections of code in b/w a capture, measures the delta from last split.      
3. <b> LogToken</b> : Tracks noncontiguous captures.      
4. <b> Execution Path </b> : An abstraction to route the execution inside the library . Custom paths can be written for user additional add on functionalities and could be installed on different build versions.    
5. <b> Thread Locks </b> : Thread starting the measurement should end it.    
  
# Usage  
  
Setup in 3 easy steps:  
  
1. Install the desired `ExecutionPath` , in the `onCreate` of your application class **as early as  possible**. Prior to that `Snippet` APIs will not have any effect as snippet ships with the  default execution path that is no-op path. For usual purposes use `MeasuredExecutionPath` 
2. Set   the filter that you would like to use in the log cat using `newFilter` method, default filter  is "**Snippet**"  
3. Set the flags that determine the amount of verbose in the logs using `addFlag` method. The flags  that Snippet supports are, `FLAG_METADATA_CLASS`, `FLAG_METADATA_METHOD`, `FLAG_METADATA_LINE`   , `FLAG_METADATA_THREAD_INFO`. Some of the filters are added by default.  
  
**Below is the sample setup code:**  
  

    if(BuildConfig.DEBUG) { 
		Snippet.install(new Snippet.MeasuredExecutionPath());      
        Snippet.newFilter("SampleFilter");      
        Snippet.addFlag(Snippet.FLAG_METADATA_LINE | Snippet.FLAG_METADATA_THREAD_INFO);      
    }   

## How to measure the a piece of code.  
  
> There are 3 ways to capture the code path.  
  
1. `Snippet.capture(Closure closure)` - For continuous section of code, pass code as lambda inside  the closure.  

2. `Snippet.startCapture()/LogToken.endCapture()` - For non contiguous sections of code possibly inside the same file . Or places where `capture(closure)` does not work as in case of some anonymous inner classes.  

4. `Snippet.startCapture(String tag)/Snippet.find(tag).endCapture()` - For code flows spanning over  
   multiple files. ex. you have started a measurement in `Application.onCreate()` and end the  
   measurement on the landing activity. Use `Snippet.startCapture(tag)` to start the measurement and  
   find the token using `Snippet.find(tag)` and call `endCapture()` on that.  
  
**Use case 1:** Code that can be passed as lambda.
  

    @Override  
    protected void onCreate(@Nullable Bundle savedInstanceState) {  
        // The capture API can be used to measure the code that can be passed as a lambda.  
        // Adding this lambda captures the class, line, thread etc automatically into the logcat.
        // This cannot be use for code that returns a value as the lambda declared for closure is 
        // is a non returning lambda. For the case that could return a value and are a little complex // use the log-token based API demonstrated below.  
        
        // Captures the code as a lambda.  
        Snippet.capture(()-> super.onCreate(savedInstanceState)); 
    }

**Use case 2:** Measurement starts from a different class and ends in a  
different class. 
Below the measurement has started in Application class and will end in an Activity class. We use TAG based API to handle this case.  
  

      public class SampleApplication extends Application {  
        @Override  
         public void onCreate() {  
            super.onCreate();  
            if(BuildConfig.DEBUG) {  
                Snippet.install(new Snippet.MeasuredExecutionPath());  
                Snippet.newFilter("SampleFilter");  
                Snippet.addFlag(Snippet.FLAG_METADATA_LINE | Snippet.FLAG_METADATA_THREAD_INFO);  
            }  

           Snippet.startCapture("app_start"); // Start the measurement in Application class  
      }  
    }
        
and ended in `MainActivity` class

    public class MainActivity extends AppCompatActivity {  

      @Override  
      protected void onCreate(@Nullable Bundle savedInstanceState) {  
            super.onCreate(savedInstanceState); 
            // End the measurement that started in the Application class  
            Snippet.find("app_start").endCapture();
      }  

      @Override  
      protected void onStart() {  
            super.onStart();  
        }  
    }
    
**Use case 3:** Using Log Tokens. Log tokens can be used within a class or method easily. 

If you need to use log tokens to measure code spread across multiple files and your measurements do not deal with multiple threads, use TAG based API discussed above. 

**LogTokens** will shine if you need to fire multiple threads at the same time and measurement is inside the common code that all the threads execute. Then create separate log token and hand over to different threads. We are working to fix this limitation.

     public class MainActivity extends AppCompatActivity {        
            
            @Override    
          protected void onCreate(@Nullable Bundle savedInstanceState) {    
                // The capture API can be used to measure the code that can be passed as a lambda.    
                // Calling startCapture gives a log token to the caller that can we used to end the measurement.   
                // The moment start capture is called, the measurement has started and when end capture will  
                // be called on the log-token, that is when the measurement will end. Endcapture can be called only once 
                // per log token.      
                     
                ILogToken token = Snippet.startCapture();    
                setContentView(R.layout.activity_main);    
                token.endCapture("Time to set the content view");    
          }  
      

Snippet capture all the context information such as class, method, thread, line number out of the box and creates pretty logs as shown below. So that you can locate your logs easily. 
On top of that if you need more verbosity, then each API has a string based overload too. You can explore that also.
 
    2021-12-11 15:11:24.197 11400-11400/com.microsoft.sample D/SampleFilter: [Class = MainActivity]|::::|[Method = onCreate]|::::|<Line no. 21>|::::|[Thread name = main]|::::||::::|(18 ms) 
    2021-12-11 15:11:24.376 11400-11400/com.microsoft.sample D/SampleFilter: Time to set the content view|::::|[Class = MainActivity]|::::|[Method = onCreate]|::::|<Line no. 29>|::::|[Thread name = main]|::::||::::|(178 ms) 
    2021-12-11 15:11:24.377 11400-11400/com.microsoft.sample D/SampleFilter: [Class = MainActivity]|::::|[Method = onCreate]|::::|<Line no. 32>|::::|[Thread name = main]|::::||::::|(295 ms)  

We can create multiple execution path implementations by extending MeasuredExecutionPath classes,  and do customised work with the data that is provided by the measured path such as logging it in remote servers, putting all the data to a DB, files etc. Check `FileExecutionPath` in the sample  app.  
  
Check out the sample app in `app/` to see the process in action.  
  
## Splits  
  
**Splits** can be defined as a logical span of code within a capture. Splits can span within same file or different files. The purpose is to double down on the focussed areas, and help in debugging.  
It could be used for seeing what is the contribution of a small portion of code within a capture and  find of problem areas.    

**It is advisable to always use splits only for debugging purposes.** **It is not advised to ship splits related code to production.** **Below is the demo on how to use splits**  
  
1. Once you get a log token using `Snippet.startCapture()` call.  
2. You can call `logtoken.addSplit()` call. It will print the amount of time that has passed since  
   the last call to `addSplit()` was made or if it is a first split then it will measure the time  
   from the call to `startCapture().`  
3. There is no limit on the number of splits that can be created, once the `endCapture()` is called,  
   snippet prints a **"Split Summary"** that shows what was the percentage of time each split take  
   with respect to the capture.  
  
*Click on the below like to check out how the split summary looks like.* [How split summary looks like?](https://ibb.co/t8qp7Tk)  
  
## ThreadLocks  
  
Thread lock is the functionality where the thread that started the measurement  
using `startCapture()` could only end it. If other thread tries to do that, an error is logged and action is skipped. It can be enabled easily like this  
  
Snippet.startCapture().enableThreadLock()  
  
## ExecutionPaths  
  
* Execution path determines how core the functionality of this library should behave.  
* It might be possible that we do not want to execute the code entirely in release builds or  
* may want to add some extra information into the existing information and add it to files.  
* We can plugin a custom execution path or method through `Snippet.install(executionPath)` method.  
  
Snippet comes with a `MeasuredExecutionPath` & `ReleaseExecutionPath` , measured path is the one that routes the the Snippet API calls to the core library functionality, and `ReleaseExecutionPath` make Snippet no-op. Release path is the default path for Snippet. User has to set the path for specific build types using `Snippet.install(executionPath)`  . Below is an example, where we set the `MeasuredExecutionPath` on DEBUG builds and `FileExecutionPath` on RELEASE builds.
  

    if(BuildConfig.DEBUG) {
         Snippet.install(new Snippet.MeasuredExecutionPath());    
         Snippet.newFilter("SampleFilter");      
    } else {      
         Snippet.install(new FileExecutionPath()); 
         Snippet.newFilter("ReleaseFilter");      
    }      
    Snippet.addFlag(Snippet.FLAG_METADATA_LINE | Snippet.FLAG_METADATA_THREAD_INFO);  

  
## Writing a custom execution path  
  
1. Extend `ExecutionPath`, in our example we will extend `MeasuredExecutionPath`.  

2. Override `ExecutionPath#capture(Closure)` and  `ExecutionPath#capture(String, Closure)` This will make sure that you are implementing a custom code for lambda based API.  
4. You need to provide a custom log token also and override the `LogToken#endCapture()`  and `LogToken#endCapture(String)` so that you can perform the custom actions on all types(contiguous/non contiguous) of code.  
5. For doing this extend ExtendableLogToken and override  `ExtendableLogToken#endCapture(String)`,  
   and  `ExtendableLogToken#endCapture()`. Once done, return the `ExtendableLogToken` instance from  `Snippet#startCapture(String)`, and  `Snippet#startCapture(String)` methods.  
  
**NOTE**: In almost all the cases, every new execution path that would be created, would require a  new extension of `ExtendableLogToken`  
  
**Sample:**  
  
          
    /**      
     * Demo for showing custom implementation of Execution Path. This path, takes the data that is * captured from the MeasuredExecutionPath and passes it to     FileExecutionPath and then the data * is written to the file. * <p>      
     * We need to override LogToken also, as for the code that is non contiguous all the measurements * are inside the log token that is handed over to the user by Snippet.startCapture() API * So if we want that our new execution to work for both kinds of APIs that ie. * The one passed through a lambda in Snippet.capture(lambda) and Snippet.startCapture()/LogToken.endCapture()
     * We need to override both the classes. */  
     
    public class FileExecutionPath extends Snippet.MeasuredExecutionPath {      
          
      @Override      
      public ILogToken startCapture(String tag) {      
            return super.startCapture(tag);      
        }      
          
     @NonNull      
     @Override  
     public ExecutionContext capture(String message, Snippet.Closure closure) {      
     	ExecutionContext context = super.capture(message, closure);      
        Log.d("Snippet", "Class: " + context.getClassName() + "Duration: " + context.getExecutionDuration());      
        // Context has all the information that measured path has captured. Use that to write to files.      
        return writeToFile(context);      
     }      
          
     private ExecutionContext writeToFile(ExecutionContext context) {      
        // Code to write to a file goes here, create a thread and write.      
        // Finally return a the execution context(could be the same or a new implementation) with some // of the details that you captured.      
        // NOTE: always put the relevant information on the context before you start doing IO // so that the execution path could return successfully.  
	return context;      
     }      
          
     @NonNull      
     @Override  public ExecutionContext capture(Snippet.Closure closure) {      
            return super.capture(closure);      
     }      
          
     // We need to return a log token implementation that writes to a file when we call endCapture()      
     // APIs. 
     // USE ExtendableLogToken for the above purpose  @Override      
     public ILogToken startCapture() {      
           return new ExtendableLogToken(super.startCapture());      
     }      
          
     @Override      
     public ILogToken find(String tag) {      
          return super.find(tag);      
     }      
          
     public class FileWritingLogToken extends ExtendableLogToken {      
          
         public FileWritingLogToken(ILogToken logToken) {      
             super(logToken);      
         }      
          
         @Override      
         public ExecutionContext endCapture(String message) {      
            ExecutionContext context = super.endCapture(message);      
            writeToFile(context);      
            return context;      
         }      
          
         @Override      
         public ExecutionContext endCapture() {      
            ExecutionContext context = super.endCapture();      
            writeToFile(context);      
            return context;      
          }      
       }    
    }     



Finally, install it at the application create,  
  
```    
if(Build.DEBUG) { 
    Snippet.install(new FileExecutionContext());
}    
````

Cheers,  
  
## Contributing  
  
This project welcomes contributions and suggestions. Most contributions require you to agree to a Contributor License Agreement (CLA) declaring that you have the right to, and actually do, grant us the rights to use your contribution. For details, visit https://cla.opensource.microsoft.com.  
  
When you submit a pull request, a CLA bot will automatically determine whether you need to  provide a CLA and decorate the PR appropriately (e.g., status check, comment). Simply follow the instructions provided by the bot. You will only need to do this once across all repos using our CLA.  
  
This project has adopted  
the [Microsoft Open Source Code of Conduct](https://opensource.microsoft.com/codeofconduct/). For more information see  
the [Code of Conduct FAQ](https://opensource.microsoft.com/codeofconduct/faq/) or      contact [opencode@microsoft.com](mailto:opencode@microsoft.com) with any additional questions or comments.  
  
## Trademarks  
  
This project may contain trademarks or logos for projects, products, or services. Authorized use of  
Microsoft trademarks or logos is subject to and must follow 
[Microsoft's Trademark & Brand Guidelines](https://www.microsoft.com/en-us/legal/intellectualproperty/trademarks/usage/general).      
Use of Microsoft trademarks or logos in modified versions of this project must not cause confusion  
or imply Microsoft sponsorship.      
Any use of third-party trademarks or logos are subject to those third-party's policies.


## Downloads
Add it in your root build.gradle at the end of repositories:
```css
allprojects {
	repositories {
	      ...
	      maven { url 'https://jitpack.io' }
	}
}
```
Add the dependency to your project and replace tag with the release tag in the git repository
```css
dependencies {
    implementation 'com.github.microsoft:snippet-timekeeper:Tag'
}
```

