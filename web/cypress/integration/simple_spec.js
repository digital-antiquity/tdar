describe('My First Test', function() {
  it('Visits the Kitchen Sink', function() {
    cy.request('GET','https://vmware-localhost:8443/')
  })
})
