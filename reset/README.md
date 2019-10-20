# RESET PASSWORD

This app handles the password reset email cycle it is and should remain entirely separate 
from the facilities used by logged in users for changing their password. These pages do not
require authentication and employ a standard email/reset pattern. 

MFA and secret questions are not provided because MFA in many cases requires an account 
with an MFA provider and secret questions are a more cumbersome experience that many
sites might not want to burden their user with (and the author of this code isn't fond of 
their susceptibility to social engineering). In any case not everyone is building 
a system that requires banking level security.

From a user perspective the process looks like this:
1. User lands on reset request page
1. User provides email
1. The user's entry is always accepted 
1. **IF** the email has an account then that email account receives mail with a reset link
1. The user clicks the reset link
1. The user provides password and confirms
1. The user is redirected to the log in page.

From a technical perspective the proccess is:
1. `GetChangePasswordLink.java` receives a request to reset a given email
1. `GetChangePasswordLink.java` validates that that email represents an account
   1. If associated with an account
      1. Write a UserSecurity object with a token but no password hash.
      1. Send email
   1. If not associated with an account silently do nothing.
1. Always respond with an identical page directing user to email
1. `ChangePassword.java` receives a token for reset
1. `ChangePassword.java` looks for UserSecurity object with token and without password hash, 
   created within 1 hour ago
    1. If not found (or token missing): redirect to try again page
    1. If found: display password entry page
1. `ChangePassword.java` receives token, password and confirmed password
1. `ChangePassword.java` looks for UserSecurity object with token and without password hash,
   created within 1 hour ago
    1. If not found (or token missing): redirect to try again page
    1. If found: 
        1. Update UserSecurity object with password hash
        1. Update UserSecurity object to remove token
        1. Update AppUser object with new UserSecurity object
1. Redirect the user to login page  
      