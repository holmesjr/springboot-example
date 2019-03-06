const sum = require('./sum');

describe('When doing addition', () =>{
    it('adds 1 + 2 to equal 3', () => {
        expect(sum(1, 2)).toBe(3);
    });

    it('adds 3 + 2 to equal 5', () => {
        expect(sum(3, 2)).toBe(5);
    });
});


