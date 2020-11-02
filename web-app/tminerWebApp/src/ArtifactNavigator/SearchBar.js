import React from "react";

const SearchBar = ({ label, isSelected, onCheckboxChange }) => (
  <div>
    <label>
      <input
        type="text"
        name={label}
        checked={isSelected}
        onChange={onCheckboxChange}
        className="form-check-input"
      />
      {label}
    </label>
  </div>
);

export default SearchBar;
