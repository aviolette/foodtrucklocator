<!DOCTYPE html>
<html lang="en">
<head>
  <meta charset="utf-8">
  <meta http-equiv="X-UA-Compatible" content="IE=edge">
  <meta name="viewport" content="width=device-width, initial-scale=1">
  <meta name="description" content="Login page">
  <meta name="author" content="Andrew Violette, @aviolette/@chifoodtruckz on twitter"/>
  <link rel="icon" href="../../favicon.ico">

  <title>Login</title>

  <%@ include file="../include/bootstrap_css.jsp" %>

  <link href="/css/signin.css" rel="stylesheet">

</head>

<body>

<div class="container">

  <form class="form-signin" method="post" action="">
    <h2 class="form-signin-heading">Sign-in</h2>
    <label for="inputEmail" class="sr-only">Email address</label>
    <input type="email" id="inputEmail" class="form-control" placeholder="Email address" name="email" required
           autofocus>
    <label for="inputPassword" class="sr-only">Password</label>
    <input type="password" id="inputPassword" class="form-control" placeholder="Password" name="password" required>
    <button class="btn btn-lg btn-primary btn-block" type="submit">Sign in</button>
    <a href="#">Forgot password?</a>
  </form>

  <form class="form-signin" style="padding-top:10px">
    <p>or</p>
    <a class="btn btn-lg btn-success btn-block" href="/book/create_account">Create an Account</a>
  </form>

<%@ include file="../footer.jsp"%>