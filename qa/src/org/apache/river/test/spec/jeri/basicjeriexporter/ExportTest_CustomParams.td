testClass=ExportTest_CustomParams
testCategories=jeri,jeri_spec
# This test fails on the JGDMS modular build because the AccessControlContext 
# in force when the service is exported is different, than when the 
# InvocationDespatcher dispatch method is called.  This occurs because JERI is
# in a separate module than the platform, the platform classes aren't added to
# the stack context until after export and before
# the dispatcher is called and are thus missing from the AccessControlContext.
# This is not due to lazy class loading etc, it's just the code in the platform
# jar isn't on the call stack, changing this would require changing the call
# stack by way of modifying the methods and objects called during the test.
# Originally jeri was included in the platform jar.