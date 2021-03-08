exports = function(...args) {
    return parseInt(args.reduce((a,b) => a + b, 0));
};
