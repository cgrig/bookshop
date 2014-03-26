function validate_password(p1, p2) {
  if (p1.value != p2.value) {
    alert("Passwords don't match."); 
    p1.focus();
    return false;
  }
  return true;
}

function validate_field(f) {
  if (f.value == null || f.value == "") {
    alert("Field " + f.name + " should not be empty.");
    f.focus();
    return false;
  }
  if (f.value.match("^[a-zA-Z0-9.@]+$") == null) {
    alert("Please use only letters, digits, '.', '@', and no spaces.\n"
      + "See field " + f.name + ".");
    f.focus();
    return false;
  }
  return true;
}

function validate_form(f) {
  return validate_password(f.password, f.again) &&
    validate_field(f.username) &&
    validate_field(f.password) &&
    validate_field(f.again) &&
    validate_field(f.firstName) &&
    validate_field(f.lastName) &&
    validate_field(f.email) &&
    validate_field(f.snum);
}
