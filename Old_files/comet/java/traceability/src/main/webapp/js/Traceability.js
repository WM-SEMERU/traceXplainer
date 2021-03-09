Q(function() {
    //appends an "active" class to .popup and .popup-content when the "Open" button is clicked
    Q(".open").on("click", function(){
      Q(".popup-overlay, .popup-content").addClass("active");
    });

    //removes the "active" class to .popup and .popup-content when the "Close" button is clicked
    Q(".close, .popup-overlay").on("click", function(){
      Q(".popup-overlay, .popup-content").removeClass("active");
    });
})
