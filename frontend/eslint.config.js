export default [
  {
    files: ['src/**/*.ts'],
    ignores: ['dist/**', 'build/**', 'coverage/**', 'node_modules/**', '**/*.min.js'],
    rules: {
      'no-restricted-imports': [
        'error',
        {
          patterns: ['../service/**', '../../service/**', '../../..//src/main/java/**']
        }
      ]
    }
  }
];
