import Quill from "quill";




// Optional: if you want formula or syntax, Quill already supports them



export const QUILL_MODULES = {
  table:true,
  toolbar: [
    // Row 1 — Text formatting
      ['bold', 'italic', 'underline', 'strike'],        // toggled buttons
  ['blockquote', 'code-block'],
  ['link', 'image', 'video', 'formula'],
    [{ header: 1 }, { header: 2 }, { header: 3 }],
    [{ font: [] }],
    [{ size: ['small', false, 'large', 'huge'] }],
     [{ 'header': [1, 2, 3, 4, 5, 6, false] }],
    [{ align: [] }],
    [{ direction: 'rtl' }],

    // Row 2 — Colors & background
    [{ color: [] }, { background: [] }],

    // Row 3 — Scripts & block formats
    [{ script: 'sub' }, { script: 'super' }],
    ['blockquote', 'code-block'],

    // Row 4 — Lists & indentation
    [{ list: 'ordered' }, { list: 'bullet' }, { 'list': 'check' }],
    [{ indent: '-1' }, { indent: '+1' }],

    // Row 5 — Media
    ['link', 'image', 'video', 'formula'],

    // Row 6 — Tables (requires quill-better-table)
    ['table'],

    // 
  ],

  clipboard: {
    matchVisual: false,
    matchers: [], // Custom matchers if needed
  },

  history: {
    delay: 1000,
    maxStack: 100,
    userOnly: true,
  },

//   syntax: true,       // Code syntax highlighting (highlight.js)
//   formula: true,      // Math formulas (MathJax)
//   keyboard: true,     // Custom keyboard shortcuts
 // imageResize: {},    // Resize images in editor

//   'better-table': {   // Table module
//     operationMenu: {
//       items: {
//         unmergeCells: true,
//         insertRowAbove: true,
//         insertRowBelow: true,
//         insertColumnLeft: true,
//         insertColumnRight: true,
//         deleteRow: true,
//         deleteColumn: true,
//         deleteTable: true,
//       },
//     },
//   },

};
