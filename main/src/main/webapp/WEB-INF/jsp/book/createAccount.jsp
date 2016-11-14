<%@ include file="../header.jsp"%>

<div class="row">
  <div class="col-md-12">
    <form role="form" action="" method="post">

      <div class="form-group">
        <label for="firstName">First Name</label>
        <input type="text" class="form-control" id="firstName"/>
      </div>

      <div class="form-group">
        <label for="lastName">Last Name</label>
        <input type="text" class="form-control" id="lastName"/>
      </div>

      <div class="form-group">
        <label for="email">Email</label>
        <input placeholder="foo@bar.com" type="text" class="form-control" id="email"/>
      </div>

      <div class="form-group">
        <label for="password">Password</label>
        <input type="password" class="form-control" id="password"/>
      </div>

      <div class="form-group">
        <label for="confirmPassword">Confirm Password</label>
        <input type="password" class="form-control" id="confirmPassword"/>
      </div>

      <button type="submit" class="btn btn-lg btn-primary btn-block">Create</button>

    </form>

  </div>
</div>

<%@ include file="../footer.jsp"%>